package com.example.demo1228_2.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.Vo.Address;
import com.example.demo1228_2.Vo.Alipay;
import com.example.demo1228_2.Vo.ProductUpdateByListVo;
import com.example.demo1228_2.config.*;
import com.example.demo1228_2.dto.BuylistDto;
import com.example.demo1228_2.dto.BuylistListAddressDto;
import com.example.demo1228_2.entity.Buylist;
import com.example.demo1228_2.entity.Order;
import com.example.demo1228_2.entity.Product;
import com.example.demo1228_2.entity.User;

import com.example.demo1228_2.mapper.UserMapper;

import com.example.demo1228_2.service.IBuylistService;
import com.example.demo1228_2.service.IOrderService;
import com.example.demo1228_2.service.IProductService;
import com.example.demo1228_2.service.impl.DelayQueueService;
import com.example.demo1228_2.service.impl.HttpService;
import com.example.demo1228_2.service.impl.ProductServiceImpl;
import com.example.demo1228_2.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


@RestController
@RequestMapping("/user")
@Slf4j // 自动生成log对象
public class UserController {

    @Autowired
    //UserMapper usermapper;
    UserMapper usermapper;

    @GetMapping("/name") // 查名字
    public R<String> FindUserName(HttpSession session){
        Object object= session.getAttribute("LoginName");
        String name = "";
        if(object!=null)name = (String)object;
        log.info("查名字:{}",name);
        // 有微信名返回微信名字优先
        if(session.getAttribute("Wechat_nickname")!=null)
            return R.success(session.getAttribute("Wechat_nickname").toString());
        return R.success(name);
    }

    @GetMapping("/info") // 查当前用户信息
    public R<User> FindUserInfo(HttpSession session){
        User user_result;
        Object object= session.getAttribute("LoginName");
        String name = "";
        if(object!=null){
            name = (String)object;
            // 创造筛选条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            // 有这个名字吗
            queryWrapper.eq(User::getName,name);
            try {
                user_result = usermapper.selectOne(queryWrapper);
            }catch (Exception e){
                log.info("数据库有多个重名用户");
                return R.error("数据库有多个重名用户");
            }
            log.info("查询user信息成功");
            return R.success(user_result);
        }else{
            log.info("session无名字，原因未知");
            return R.error("session无名字，原因未知");
        }
    }

    @GetMapping("/getCaptch") // 返回验证码
    public ResponseEntity<byte[]> getCaptcha(HttpServletResponse response,HttpSession session) throws IOException {
        CaptchaGenerator generator = new CaptchaGenerator();
        DefaultKaptcha kaptcha = generator.createKaptcha();
        String text = kaptcha.createText();  // 生成验证码文本

        // 通常这里还会将验证码文本存储在Session中，以便验证用户输入
        // request.getSession().setAttribute("CAPTCHA_KEY", text);
        session.setAttribute("captch",text);

        BufferedImage image = kaptcha.createImage(text);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        // 清除缓存
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/png");

        log.info("验证码：{}",text);
        return new ResponseEntity<>(baos.toByteArray(), HttpStatus.OK);
    }

    /**
     *  申请邮箱登录 发送邮件 同时添加redis 返回发送结果
     * @param email 1
     * @return 1
     */
    @GetMapping("/sendEmail")
    public R<String> UserLogin2(@RequestParam String email){
        if(!Tool.isValidEmail(email))return R.error("不是有效邮箱");

        try{
            // 生成六位随机数
            int sixDigitNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
            String content = "<tbody>\n" +
                    "        <tr>\n" +
                    "            <td>\n" +
                    "                <div style=\"background:#fff\">\n" +
                    "                    <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                    "                        <thead>\n" +
                    "                        <tr>\n" +
                    "                            <td valign=\"middle\" style=\"padding-left:30px;background-color:#415A94;color:#fff;padding:20px 40px;font-size: 21px;\">西巴商城</td>\n" +
                    "                        </tr>\n" +
                    "                        </thead>\n" +
                    "                        <tbody>\n" +
                    "                        <tr style=\"padding:40px 40px 0 40px;display:table-cell\">\n" +
                    "                            <td style=\"font-size:24px;line-height:1.5;color:#000;margin-top:40px\">邮箱验证码</td>\n" +
                    "                        </tr>\n" +
                    "                        <tr>\n" +
                    "                            <td style=\"font-size:14px;color:#333;padding:24px 40px 0 40px\">\n" +
                    "                                "+ email +"&ensp;您好！\n" +
                    "                                <br  />\n" +
                    "                                <br  />\n" +
                    "                                您的验证码是： &ensp;<div style=\"display: inline-block;border:5px solid beige;background-color: beige;\">"+ sixDigitNumber +"</div>&ensp; ，请在 30 分钟内进行验证。如果该验证码不为您本人申请，请无视。\n" +
                    "                            </td>\n" +
                    "                        </tr>\n" +
                    "                        <tr style=\"padding:40px;display:table-cell\">\n" +
                    "                        </tr>\n" +
                    "                        </tbody>\n" +
                    "                    </table>\n" +
                    "                </div>\n" +
                    "                <div>\n" +
                    "                    <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                    "                        <tbody>\n" +
                    "                        <tr>\n" +
                    "                            <td style=\"padding:20px 40px;font-size:12px;color:#999;line-height:20px;background:#f7f7f7\"><a href=\"https://www.yjztest.top/xiba-shop/\" style=\"font-size:14px;color:#929292\">返回商城</a></td>\n" +
                    "                        </tr>\n" +
                    "                        </tbody>\n" +
                    "                    </table>\n" +
                    "                </div></td>\n" +
                    "        </tr>\n" +
                    "        </tbody>";


            Tool.sendEmail(email,"西巴网-商城登录注册验证码",content);
            log.info("给{}发送的邮件发送成功，验证码{}",email,sixDigitNumber);
            // redis 设置验证码 过期时间30min
            try (Jedis jedis = new Jedis("localhost")) {
                //jedis.set("key", "value");
                jedis.setex(email, 30*60, Integer.toString(sixDigitNumber)); // 键“key”在300秒后过期
                //String value = jedis.get("key");
                //jedis.expire("key", 300);
                log.info("redis设置key:{},value:{}",email,jedis.get(email));
            } catch (Exception e) {
                log.info("redis异常: " + e.getMessage());
                throw new CustomException("redis异常:"+ e.getMessage());
            }
        }catch (Exception e){
            log.info("异常：{}",e.getMessage());
            return R.error(e.getMessage());
        }
        return R.success("success");
    }

    @GetMapping("/page") // 分页查询 接收params //防空设默认
    public R<Page<User>> FindPageUser(@RequestParam(defaultValue = "-1")int currentPage,
                                      @RequestParam(defaultValue = "-1")int PageSize){
        try {
            // 空参数抛异常
            if(currentPage == -1 || PageSize == -1 )throw new CustomException("分页查询参数为空");
            // 分页查询
            Page<User> page = new Page<>(currentPage, PageSize);

            // 创建LambdaQueryWrapper实例
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            // 根据id从低到高排序
            queryWrapper.orderByDesc(User::getCreate_time);

            // 执行查询
            Page<User> res = usermapper.selectPage(page, queryWrapper);

            /*
            //控制台打印json
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(res);
            System.out.println(json);
            //
            */
            log.info("分页查询成功");
            return R.success(res);
        }catch(Exception e){
            log.info("分页查询失败：{}",e.getMessage());
            return R.error(e.getMessage());
        }


    }

    @PostMapping("/login") // 登录  //session IsLogin判断状态
    public R<String> UserLogin(@RequestBody String jsonString,HttpSession session){
        JSONObject jsonObject = JSONObject.parseObject(jsonString);

        User user = jsonObject.getObject("user",User.class);
        String captch = jsonObject.getString("captch");
        // 登录前检查
        try{
            userService.login_check_before(captch,session);
        }catch (Exception e){
            return R.error(e.getMessage());
        }
        return userService.login(user,session);

    }


    @Autowired
    HttpService httpService;
    @Autowired
    GlobalProperties globalProperties;
    /**
     * 微信登录 前端来code 我们发secret去验证 然后登录
     * @param params 1
     * @param session 1
     * @return 1
     */
    @PostMapping("/loginByWechat")
    public R<String> UserLogin2(@RequestBody Map<String,String> params,HttpSession session){
        log.info(params.get("code"));
        log.info(params.get("state"));
        log.info(params.get("is_mobile"));
        log.info("收到loginByWechat，code:{}",params.get("code"));

        // 非重复登录判断
        if(session.getAttribute("IsLogin")!=null){
            String loginname = "";
            if(session.getAttribute("LoginName")!=null)
                loginname = (String)session.getAttribute("LoginName");

            log.info("{}:已登录，不能重复登录",loginname);
            return R.error("已登录，不能重复登录");
        }


        try{
            String code = params.get("code");
            Map<String,String> res = new HashMap<>();
            String url = "";
            // 区分服务号号 和网页应用授权（手机是服务号）
            if(params.get("is_mobile").equals("true")){
                // 服务号
                url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=wxbc1cabc1fa496099&secret="
                        + globalProperties.WECHAT_SECRET_FWH +
                        "&code=" + code + "&grant_type=authorization_code";
            }else{
                // 网页
                url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=wxa631fd7ca89e35a9&secret="
                        + globalProperties.WECHAT_SECRET +
                        "&code=" + code + "&grant_type=authorization_code";
            }
            // 拿token
            res = httpService.sendGet(url);
            JSONObject body = JSONObject.parseObject(res.get("body"));
            if(body.get("errcode")!=null)return R.error(body.getString("errmsg"));
            String access_token = body.getString("access_token");
            String openid = body.getString("openid");
            // 拿用户信息 access_token unionid
            url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access_token + "&openid=" + "openid";
            res = httpService.sendGet(url);
            body = JSONObject.parseObject(res.get("body"));
            if(body.get("errcode")!=null)return R.error(body.getString("errmsg"));
            String wechat_unionid = body.getString("unionid");
            // 不返回200 表示验证失败 用户没授权？过期？
            if(!res.get("statusCode").equals("200"))
                return R.error(res.get("statusCode")+":"+res.get("body"));

            // 如果已有账号 更新名字，头像 设置为登录状态；没有就注册
            User user = Db.lambdaQuery(User.class).eq(User::getWechat_unionid,wechat_unionid).one();
            if(user!=null){
                user.setWechat_nickname(body.getString("nickname"));
                user.setWechat_headimgurl(body.getString("headimgurl"));
                usermapper.updateById(user);
                userService.setLoginSession(user,session);
                return R.success("微信登录成功").add("username",user.getWechat_nickname());
            }else{
                String name = "微信用户："+UUID.randomUUID();
                String password = UUID.randomUUID().toString();
                user = new User();
                user.setName(name);
                user.setPassword(Tool.encode(password));
                user.setWechat_nickname(body.getString("nickname"));
                user.setWechat_headimgurl(body.getString("headimgurl"));
                user.setWechat_unionid(body.getString("unionid"));

                // 如果重复 多试几次（基本不可能重复）
                int count =0;
                while(count<=3)
                    try{
                        userService.regisByWeChatAndLogin(user,session);
                        break;
                    }catch (Exception e){
                        count++;
                    }

                user.setPassword(password);
                return userService.login(user,session);
            }


        }catch (Exception e){
            return R.error("异常:{}"+e.getMessage());
        }

    }

    @PostMapping("/loginByEmail") // 邮箱验证码登录
    public R<String> UserLogin3(@RequestBody Map<String,String> params,HttpSession session){
        String email = params.get("email");
        String code = params.get("code");
        log.info(params.get("email"));
        log.info(params.get("code"));
        log.info("收到loginByEmail，code:{},mail:{}",params.get("code"),params.get("email"));

        // 非重复登录判断
        if(session.getAttribute("IsLogin")!=null){
            String loginname = "";
            if(session.getAttribute("LoginName")!=null)
                loginname = (String)session.getAttribute("LoginName");

            log.info("{}:已登录，不能重复登录",loginname);
            return R.error("已登录，不能重复登录");
        }

        try{
            if(email == null || code == null)throw new CustomException("参数为空");

            try (Jedis jedis = new Jedis("localhost"))
            {
                // redis拿验证码
                String redis_mail_code = jedis.get(email);
                // 验证码是否正确
                // 是
                if(redis_mail_code!=null && redis_mail_code.equals(code)){
                    // 是否注册
                    if(Db.lambdaQuery(User.class).eq(User::getName,email).exists()){
                        //是 1登录状态 2删redis
                        userService.setLoginSession(Db.lambdaQuery(User.class).eq(User::getName,email).one(),session);
                        jedis.del(email);
                        return R.success("登录成功");
                    }else{
                        //否 1注册
                        if(!userService.regis(new User(email,UUID.randomUUID().toString())).getCode().equals(1))
                            return R.error("注册失败");
                        // 2登录状态
                        userService.setLoginSession(Db.lambdaQuery(User.class).eq(User::getName,email).one(),session);
                        // 3删redis
                        jedis.del(email);
                        return R.success("登录成功");
                    }

                // 否
                }else{
                    throw new CustomException("验证码错误");
                }

            } catch (Exception e) {
                log.info("" + e.getMessage());
                throw new CustomException(""+ e.getMessage());
            }
        }catch (Exception e){
            return R.error(e.getMessage());
        }
    }


    @GetMapping("/logout") // 登出
    public R<String> UserLogout(HttpSession session){
        // 断是否登录 //其实未登录会被拦截器拦截 就不用判断
        if(session.getAttribute("IsLogin")!=null){
            String loginname = "";
            if(session.getAttribute("LoginName")!=null)
                loginname = (String)session.getAttribute("LoginName");
            session.removeAttribute("IsLogin");
            session.removeAttribute("LoginName");
            log.info("{}:退出登录成功",loginname);
            session.invalidate(); // 移除全部session
            return R.success("已登出");
        }else{
            log.info("未登录，无法登出");
            return R.error("未登录，无法登出");
        }


    }

    @PostMapping("/regis") // 增加user
    public R<String> AddUser(@RequestBody User user){
        return userService.regis(user);
    }

    @PutMapping("/update") // 更新user
    public R<String> UserInfoChange(@RequestBody User user,HttpSession session){

        if(user.getId().equals(Long.parseLong("1766859847220883457")))
            return R.error("visitor角色数据锁定，不允许更改");

        log.info("{}",user);
        User db_user = usermapper.selectById(session.getAttribute("IsLogin").toString());

        // 未更改任何数据
        if(db_user.equals(user))return R.error("未改动任何数据");
        // 不能改角色
        if(!Objects.equals(db_user.getRole(), user.getRole()))return R.error("不能更改角色");

        // 不能改钱数 //這裡不能用equals 不然100.00比100返回false
        if (db_user.getMoney().compareTo(user.getMoney()) != 0)return R.error("不能更改钱数");

        // 不能改版本
        if(!Objects.equals(db_user.getVersion(), user.getVersion()))return R.error("不能更改版本");

        // 用.equals(回报空异常
        //if (!db_user.getWechat_nickname().equals(user.getWechat_nickname()))return R.error("不能更改微信数据");
        if (!Objects.equals(db_user.getWechat_nickname(), user.getWechat_nickname()))
            return R.error("不能更改微信数据");
        if (!Objects.equals(db_user.getWechat_unionid(), user.getWechat_unionid()))
            return R.error("不能更改微信数据");
        if (!Objects.equals(db_user.getWechat_headimgurl(), user.getWechat_headimgurl()))
            return R.error("不能更改微信数据");

        // 创建时间和id不能改
        // 在 Java 中，!= 和 == 运算符用于比较两个对象的引用，而不是它们的值
        if(!db_user.getCreate_time().equals(user.getCreate_time())){
            log.info(db_user.getCreate_time().toString());
            log.info(user.getCreate_time().toString());
            return R.error("不能更改创建时间");
        }
        if(!db_user.getId().equals(user.getId()))return R.error("不能更改Id");

        // (如果不是本名，改名字了)查数据库有没相同名
        if(!Objects.equals(session.getAttribute("LoginName").toString(), user.getName())){
            // 创造筛选条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            // 有这个名字吗
            queryWrapper.eq(User::getName,user.getName());
            // 有相同名就不能更新 更新数据库也会报错
            if(usermapper.selectOne(queryWrapper)!=null)return R.error("名字已存在");
        }

        // 改密码了 sha256化
        if(!db_user.getPassword().equals(user.getPassword()))
            user.setPassword(Tool.encode(user.getPassword()));

        // 注入数据库 防数据和数据库长度不对 抛异常
        try{
            int num = usermapper.updateById(user);
            if(num==0)throw new CustomException("数据库updatebyid失败，返回0");
            String res = "成功更新"+num +"行";
            log.info(res);
            // 如果更新成功
            session.setAttribute("LoginName",user.getName());
            return R.success(res);
        }catch (Exception e){
            log.info(e.getMessage());
            return R.error(e.getMessage());
        }

    }

    @GetMapping("/session")
    public List<String> UserInfoChange(HttpSession session){
        // 获取Session中所有的属性名
        Enumeration<String> attributeNames = session.getAttributeNames();
        List<String> res = new ArrayList<>();
        // 遍历所有的属性名，打印出每个属性的名字和值
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            Object value = session.getAttribute(name);
            log.info(name + ": " + value);
            res.add(name + ": " + value);
        }
        int interval = session.getMaxInactiveInterval()/ (60 * 60 * 24);;
        log.info("Session超时时间：" + interval + "天,");
        res.add("Session超时时间：" + interval + "天,");
        return res;
    }


    @Autowired
    IOrderService orderService;

    @Autowired
    ProductServiceImpl productService;

    @Autowired
    IBuylistService buylistService;

    @PostMapping("/build/order") // 建订单  0. 商家库存有无 1.建订单 2.状态未支付 3.删车
    public R<String> UserPay(@RequestBody BuylistListAddressDto buylistListAddressDto, HttpSession session){
        // 显示传递元素
        //params.forEach((key, value) -> log.info(key + ": " + value));
        // 最终建表order的info
        List<BuylistDto> buylistDtoLists = buylistListAddressDto.getBuylistDtoLists();
        List<BuylistDto> legal_buylistDtoLists = new ArrayList<>();

        long threadId = Thread.currentThread().getId(); // 获取当前线程的ID
        Alipay alipay = new Alipay(); // 前端要的数据

        // 商品表
        Set<Product> productSet = new HashSet<>();

        try{
            if(buylistDtoLists.isEmpty())throw new CustomException("参数为空");
            // 购物车防伪造 导致操作他人购物车
            Set<Long> BuylistIds = new HashSet<>();

            // 先拿buylist ids 再判断防伪
            for(BuylistDto buylistDto : buylistDtoLists){
                BuylistIds.add(buylistDto.getBuylist().getId());
            }

            List<Buylist> db_BuylistList = buylistService.listByIds(BuylistIds);

            // 总价格
            BigDecimal TotalMoney = BigDecimal.valueOf(0);;
            // 总数量
            int TotalNum = 0;
            // 看有没有掺杂他人订单 // 商品有没卖完
            for(Buylist buylist:db_BuylistList){
                Product product = productService.getById(buylist.getProduct_id());

                if(!buylist.getUser_id().equals(Long.parseLong(session.getAttribute("IsLogin").toString())))
                    throw new CustomException("不是本人订单");
                if(product.getNum()==0)
                    throw new CustomException("商品："+product.getName()+"已经卖完，请到购物车去除该商品");

                //商品减去(被买数) 加销量
                product.setNum(product.getNum()-buylist.getProduct_num());
                product.setSold_num(buylist.getProduct_num());
                productSet.add(product);


                // 构造Dto
                BuylistDto buylistDto = new BuylistDto();
                buylistDto.setBuylist(buylist);
                buylistDto.setProduct(product);
                legal_buylistDtoLists.add(buylistDto);


                // 计算过程中的一个步骤
                BigDecimal productNum = new BigDecimal(buylist.getProduct_num());
                TotalMoney = TotalMoney.add(product.getPrice().multiply(productNum));
                TotalNum += buylist.getProduct_num();
            }
            // 确保最终结果保留两位小数，使用四舍五入
            TotalMoney = TotalMoney.setScale(2, RoundingMode.HALF_UP);

            Order order = new Order();
            order.setUser_id(Long.parseLong(session.getAttribute("IsLogin").toString()));
            order.setInfo(legal_buylistDtoLists);
            order.setStatus("未支付");
            order.setTotalMoney(TotalMoney);
            order.setTotalNum(TotalNum);
            order.setAddress(buylistListAddressDto.getAddress());

            // 乐观锁抢购 删Product库存and建订单（出问题就回滚）
             // set转list
            List<Product> productList = new ArrayList<>(productSet);
             // 批量update 遇到版本号不对的回滚
            int attempt = 1;
             // 版本号不对时尝试3次 //同时检查库存0
            while(attempt<=3){
                try{
                    // 构建Vo
                    ProductUpdateByListVo productUpdateByListVo = new ProductUpdateByListVo();
                    productUpdateByListVo.setProductList(productList);
                    productUpdateByListVo.setOrder(order);
                    productUpdateByListVo.setBuylistIds(BuylistIds);
                    // 前端需要的一些数据
                    alipay = productService.updateByList(productUpdateByListVo);
                    // 版号正确 商品数量已经全部更新
                    log.info("线程{}：第{}次尝试，版号正确，商品数量已经全部更新",threadId,attempt);
                    break;
                }catch(Exception e){
                    if(attempt==3){
                        // 3次失败时抛出 //
                        log.info("线程{}：错误3次，停止尝试，退出返回",threadId);
                        throw new CustomException(e.getMessage());
                    }

                    log.info("线程{}：错误回滚，第{}次异常:{}",threadId,attempt,e.getMessage());
                    // 重新获取商品表 //更新版号 // 同时检查库存0否
                    List<Product> new_productList = new ArrayList<>();
                    for(Product product:productList){
                        if(product.getNum()==0){
                            log.info("线程{}：商品{}卖完了",threadId,product.getName());
                            throw new CustomException("商品"+product.getName()+"卖完了，请重新从购物车结账");
                        }
                        new_productList.add(productService.getById(product.getId()));
                    }
                    productList = new_productList;
                }
                attempt++;

                // 随机睡觉50-250
                Random rand = new Random();
                // Generate a random number between 50 and 250
                int randomSleepTime = 50 + rand.nextInt(201); // 201 is the bound (250 - 50 + 1)
                Thread.sleep(randomSleepTime);
            }
            // 到这应该成功了？
            log.info("线程{}似乎成功1.减库存2.建订单3.删购物车了",threadId);
            // 注入Alipay给前端（跳转支付宝的信息）
            return R.success("订单创建成功").add("alipay",alipay);

        }catch (Exception e){
            log.info("线程{}异常：{}",threadId,e.getMessage());
            return R.error(e.getMessage());
        }

    }

    @PostMapping("/build/order_for_one") // 为同种商品，立即支付的，建订单  0. 商家库存有无 1.建订单 2.状态未支付
    public R<String> UserPay2(@RequestBody BuylistListAddressDto buylistListAddressDto, HttpSession session){

        List<BuylistDto> buylistDtoLists = buylistListAddressDto.getBuylistDtoLists();
        Address address = buylistListAddressDto.getAddress();
        long threadId = Thread.currentThread().getId(); // 获取当前线程的ID
        Alipay alipay = new Alipay(); // 前端要的数据

        try {
            if (buylistDtoLists.isEmpty())throw new CustomException("参数1为空");
            if (address == null)throw new CustomException("参数2为空");

            // 就是为了拿商品id + 设定时间 顾客要买的数量
            BuylistDto buylistDto = buylistDtoLists.get(0);
            Buylist buylist = buylistDto.getBuylist();
            buylist.setCreate_time(LocalDateTime.now());
            buylistDto.setBuylist(buylist);
            Long productId = buylistDto.getBuylist().getProduct_id();
            BigDecimal productNum = new BigDecimal(buylistDto.getBuylist().getProduct_num());
            // 防止数据造假 从数据库检验
            Product db_product = productService.getById(productId);
            if(db_product == null)throw new CustomException("参数3为空");

            // 算总价 计算过程中的一个步骤
            BigDecimal totalMoney = db_product.getPrice().multiply(productNum);

            // 构造Dto 防造假
            BuylistDto legal_buylistDto = new BuylistDto();
            legal_buylistDto.setBuylist(buylistDto.getBuylist());
            legal_buylistDto.setProduct(db_product);
            List<BuylistDto> legal_buylistDtoLists = new ArrayList<>();
            legal_buylistDtoLists.add(legal_buylistDto);



            // region 构建订单
            Order order = new Order();
            order.setUser_id(Long.parseLong(session.getAttribute("IsLogin").toString()));
            order.setInfo(legal_buylistDtoLists);
            order.setStatus("未支付");
            order.setTotalMoney(totalMoney);
            order.setTotalNum(buylistDto.getBuylist().getProduct_num());//
            order.setAddress(buylistListAddressDto.getAddress());
            // endregion

            // region构建params
            Map<String,Object> params = new HashMap<>();
            params.put("order",order);
            params.put("db_product",db_product);
            // endregion

            // 这里防止高并发 乐观锁尝试5次
            int attempt = 1;
            while(attempt<=5){
                log.info("线程{}第{}次尝试..",threadId,attempt);
                try{
                    alipay = productService.updateByOne(params);
                    // 无异常退出while
                    log.info("线程{}第{}次无异常成功结束",threadId,attempt);
                    return R.success("订单创建成功").add("alipay",alipay);
                }catch (Exception e){
                    log.info("线程{}第{}次异常：{}",threadId,attempt,e.getMessage());
                    // region库存不足，或其他异常，再抛结束
                    if(!e.getMessage().equals("版本号冲突"))
                        throw new CustomException(e.getMessage());
                    // endregion

                    // region随机睡觉50-250
                    Random rand = new Random();
                    // Generate a random number between 50 and 250
                    int randomSleepTime = 50 + rand.nextInt(201); // 201 is the bound (250 - 50 + 1)
                    Thread.sleep(randomSleepTime);
                    // endregion

                    // region如果是版本号异常 说明并发冲突 更新版本号重试
                    db_product = productService.getById(productId);
                    params.put("db_product",db_product);
                    // endregion

                    attempt++;
                }
            }




        }catch (Exception e){
            // 这里是参数为空异常
            return R.error(e.getMessage());
        }
        // 这里是尝试超过五次
        log.info("线程{}，5次异常踢出-----",threadId);
        return R.error("服务器繁忙，请稍后重试");
    }

    @PostMapping("/addonebyadmin") // 管理員增加新用戶
    public R<String> UserAdd(@RequestBody User user,HttpSession session){
        try{
            log.info("{}",user);
            // 验证权限
            if(!session.getAttribute("Role").toString().equals("admin"))
                throw new CustomException("不是管理员，禁止操作");

            // 防空
            if(user.getName()==null || user.getPassword()==null)
                throw new CustomException("用户或密码不能为空");

            //名字防重复
            if(Db.lambdaQuery(User.class).eq(User::getName,user.getName()).one()!=null)
                throw new CustomException("名字已存在，添加失败");

            user.setCreate_time(null);
            user.setVersion(null);
            user.setId(null);
            user.setPassword(Tool.encode(user.getPassword())); // 密码记得sha256化

            if(usermapper.insert(user)!=1)
                throw new CustomException("数据库插入失败，返回0");

            return R.success("添加成功");

        }catch (Exception e){
            return R.error(e.getMessage());
        }
    }

    @Autowired
    UserServiceImpl userService;

    @PostMapping("/deletelistbyadmin") // 管理員批量删除用户
    public R<String> UserDelete(@RequestBody List<User> users,HttpSession session){
        try{
            log.info("{}",users);
            // 验证权限
            if(!session.getAttribute("Role").toString().equals("admin"))
                throw new CustomException("不是管理员，禁止操作");

            // 防空
            if(users==null)
                throw new CustomException("删除列表不能为空");

            List<Long> Ids = new ArrayList<>();
            for(User user:users){
                Ids.add(user.getId());
            }

            if(userService.removeBatchByIds(Ids))return R.success("删除成功");
            else return R.error("删除失败（或者部分失败）");

        }catch (Exception e){
            return R.error(e.getMessage());
        }
    }

    @PutMapping("/updateonebyadmin") // 管理員修改用戶
    public R<String> Userupdate(@RequestBody User user,HttpSession session){
        try{
            log.info("{}",user);
            // 验证权限
            if(!session.getAttribute("Role").toString().equals("admin"))
                throw new CustomException("不是管理员，禁止操作");

            // 防空
            if(user.getName()==null || user.getPassword()==null)
                throw new CustomException("用户或密码不能为空");

            //名字防重复(不是原来的名字 且重复（和别人重复，而不是自己）)
            User db_user = usermapper.selectById(user.getId());
            if(!db_user.getName().equals(user.getName()) && Db.lambdaQuery(User.class).eq(User::getName,user.getName()).one()!=null)
                throw new CustomException("名字已存在，修改失败");

            // 改密码了 sha256化
            if(!db_user.getPassword().equals(user.getPassword()))
                user.setPassword(Tool.encode(user.getPassword()));

            if(usermapper.updateById(user)!=1)
                throw new CustomException("数据库插入失败，返回0");

            return R.success("修改成功");

        }catch (Exception e){
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/selectpagebyadmin") // 分页查询 接收params //防空设默认
    public R<Page<User>> FindPageUsers(@RequestParam Map<String, String> params, HttpSession session){
        // 使用LambdaQueryChainWrapper构建查询
        LambdaQueryChainWrapper<User> query = new LambdaQueryChainWrapper<>(usermapper);

        // 根据条件动态添加查询条件
        if (params.get("name") != null) {
            query.like(User::getName, params.get("name"));
        }
        // 单独处理startDate，如果存在则查询大于等于这个日期的记录
        if (params.get("startDate") != null) {
            query.ge(User::getCreate_time, params.get("startDate")); // ge是“greater than or equal to”的缩写
        }
        // 单独处理endDate，如果存在则查询小于等于这个日期的记录
        if (params.get("endDate") != null) {
            query.le(User::getCreate_time, params.get("endDate")); // le是“less than or equal to”的缩写
        }
        if (params.get("id") != null) {
            query.like(User::getId, params.get("id"));
        }

        Page<User> page = new Page<>(1,10);
        // 防空参数
        if(params.get("currentPage")!=null && params.get("PageSize")!=null)
            page = new Page<>(Long.parseLong(params.get("currentPage")),Long.parseLong(params.get("PageSize")));
        // 执行分页查询
        Page<User> result = query.orderByDesc(User::getCreate_time).page(page);

        // map返回筛选
        R<Page<User>> response = R.success(result);
        response.setMap(params);

        return response;
    }
}
