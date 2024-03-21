package com.example.demo1228_2.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.Vo.Alipay;
import com.example.demo1228_2.Vo.ProductUpdateByListVo;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.config.Tool;
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
import com.example.demo1228_2.service.impl.ProductServiceImpl;
import com.example.demo1228_2.service.impl.UserServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;


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
    public R<String> UserLogin(@RequestBody User user,HttpSession session){
        // 断是否登录
        if(session.getAttribute("IsLogin")!=null){
            String loginname = "";
            if(session.getAttribute("LoginName")!=null)
                loginname = (String)session.getAttribute("LoginName");

            log.info("{}:已登录，不能重复登录",loginname);
            return R.error("已登录，不能重复登录");
        }
        // 创造筛选条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 有这个名字吗
        queryWrapper.eq(User::getName,user.getName());

        // 查找
        User user_result;
        try {
            user_result = usermapper.selectOne(queryWrapper);
        }catch (Exception e){
            log.info("数据库有多个重名用户或其他："+e.getMessage());
            return R.error("数据库有多个重名用户："+e.getMessage());
        }
        // 如果查到
        if(user_result!=null){
            //如果密码一样
            if(Tool.matches(user.getPassword(),user_result.getPassword())){
                log.info("{}:密码正确，登陆成功",user.getName());

                log.info("设置session登录IsLogin为用户Id");
                session.setAttribute("IsLogin",user_result.getId());
                session.setAttribute("LoginName",user.getName());
                session.setAttribute("Role",user_result.getRole());
                session.setAttribute("LoginDate", Tool.getDateTime());

                return R.success("密码正确，登陆成功").add("username",user.getName());
            //如果不一样
            }else{
                log.info("{}:密码不正确，登陆失败",user.getName());
                return R.error("密码错误");
            }
        // 如果查不到
        }else{
            log.info("{}:用户不存在",user.getName());
            return R.error("用户不存在");
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
        // 防空
        if(user.getName()==null || user.getPassword()==null)
            return R.error("用户或密码不能为空");

        System.out.println(user);
        // 创造筛选条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 有这个名字吗
        queryWrapper.eq(User::getName,user.getName());

        // 查找
        List<User> res = usermapper.selectList(queryWrapper);
        // 如果查到
        log.info("查找结果：{}",res);
        if(res.size()!=0){
            log.info("{}用户已存在",user.getName());
            return R.error("用户已存在");
        }

        try{
            if(user.getPassword()==null)throw new CustomException("密码为空");
            User user_safe = new User();

            user_safe.setName(user.getName());
            user_safe.setPassword(Tool.encode(user.getPassword()));
            user_safe.setRole("user");

            usermapper.insert(user_safe);
        }catch (Exception e){
            log.info("注册异常:{}",e.getMessage());
            return R.error("注册失败:"+e.getMessage());
        }
        log.info("{}注册成功",user.getName());
        return R.success("注册成功");
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

                //商品减去(被买数)
                product.setNum(product.getNum()-buylist.getProduct_num());
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
            query.eq(User::getId, params.get("id"));
        }

        Page<User> page = new Page<>(1,10);
        // 防空参数
        if(params.get("currentPage")!=null && params.get("PageSize")!=null)
            page = new Page<>(Long.parseLong(params.get("currentPage")),Long.parseLong(params.get("PageSize")));
        // 执行分页查询
        Page<User> result = query.page(page);

        // map返回筛选
        R<Page<User>> response = R.success(result);
        response.setMap(params);

        return response;
    }
}
