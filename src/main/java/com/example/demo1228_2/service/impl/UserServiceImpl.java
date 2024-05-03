package com.example.demo1228_2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.Vo.Alipay;
import com.example.demo1228_2.Vo.ProductUpdateByListVo;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.config.Tool;
import com.example.demo1228_2.entity.User;
import com.example.demo1228_2.mapper.UserMapper;
import com.example.demo1228_2.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    UserMapper userMapper;

    /**
     * 对比用户名密码是否正确 设立登录状态
     * @param user 1
     * @param session 1
     * @return 1
     */
    public R<String> login(User user, HttpSession session){
        // 创造筛选条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 有这个名字吗
        queryWrapper.eq(User::getName,user.getName());

        // 查找
        User user_result;
        try {
            user_result = userMapper.selectOne(queryWrapper);
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
                // 设定登录状态
                setLoginSession(user_result,session);

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

    /**
     * 设定登录状态session
     */
    public void setLoginSession(User user_result,HttpSession session){
        session.setAttribute("IsLogin",user_result.getId()); // 用户id
        session.setAttribute("LoginName",user_result.getName());    // 姓名
        session.setAttribute("Role",user_result.getRole());  // 角色
        session.setAttribute("LoginDate", Tool.getDateTime()); // 登录时间
        if(user_result.getWechat_nickname()!=null)
            session.setAttribute("Wechat_nickname", user_result.getWechat_nickname()); // 微信名
    }

    /**
     * 注册 user要用户名 密码
     * @param user 1
     * @return 是否成功
     */
    public R<String> regis(User user){
        // 防空
        if(user.getName()==null || user.getPassword()==null)
            return R.error("用户或密码不能为空");

        if(Tool.isValidEmail(user.getName()))
            return R.error("用户名注册不能使用邮箱（您的账户可能会被重名邮箱验证登录）");

        System.out.println(user);
        // 创造筛选条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 有这个名字吗
        queryWrapper.eq(User::getName,user.getName());

        // 查找
        List<User> res = userMapper.selectList(queryWrapper);
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

            if(userMapper.insert(user_safe)!=1)
                return R.error("注册失败:数据库返回0");
        }catch (Exception e){
            log.info("注册异常:{}",e.getMessage());
            return R.error("注册失败:"+e.getMessage());
        }
        log.info("{}注册成功",user.getName());
        return R.success("注册成功");
    }

    /**
     * 微信注册登录 防名重
     * @param user 1
     *
     */
    public void regisByWeChatAndLogin(User user,HttpSession session)throws Exception{

        if(Db.lambdaQuery(User.class).eq(User::getName,user.getName()).exists())
            throw new CustomException("用户名重复");

        if(userMapper.insert(user)!=1)
            throw new CustomException("数据插入失败，返回不为1");

    }

    /**
     * 做一些登录前置检查 1不能重复登录2验证码为空3验证码错误 不抛异常就没问题 删session验证码
     * @param captch 用户输入的验证码
     * @param session 1
     * @throws CustomException 1
     */
    public void login_check_before(String captch,HttpSession session)throws CustomException{
        // 断是否登录
        if(session.getAttribute("IsLogin")!=null){
            String loginname = "";
            if(session.getAttribute("LoginName")!=null)
                loginname = (String)session.getAttribute("LoginName");

            log.info("{}:已登录，不能重复登录",loginname);
            throw new CustomException("已登录，不能重复登录");
        }
        // 是否有申请验证码
        if(session.getAttribute("captch")==null)throw new CustomException("未申请验证码");
        // 判断验证码是否正确
        if(!session.getAttribute("captch").equals(captch)){
            log.info("{}:{}",session.getAttribute("captch"),captch);
            // 错误了 要重新申请验证码
            session.removeAttribute("captch");
            throw new CustomException("验证码错误");
        }

        // 到这里就没问题 删session验证码
        session.removeAttribute("captch");

    }
}
