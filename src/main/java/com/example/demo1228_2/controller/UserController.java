package com.example.demo1228_2.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.entity.User;

import com.example.demo1228_2.mapper.UserMapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
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
            queryWrapper.orderByAsc(User::getId);

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
            log.info("数据库有多个重名用户");
            return R.error("数据库有多个重名用户");
        }
        // 如果查到
        if(user_result!=null){
            //如果密码一样
            if(Objects.equals(user_result.getPassword(), user.getPassword())){
                log.info("{}:密码正确，登陆成功",user.getName());

                log.info("设置session登录IsLogin为true");
                session.setAttribute("IsLogin",user_result.getId());
                session.setAttribute("LoginName",user.getName());

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
            return R.success("已登出");
        }else{
            log.info("未登录，无法登出");
            return R.error("未登录，无法登出");
        }


    }

    @PostMapping("/regis") // 增加user
    public R<String> AddUser(@RequestBody User user){
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
            usermapper.insert(user);
        }catch (Exception e){
            log.info("注册异常:{}",e.getMessage());
            return R.error("注册失败");
        }
        log.info("{}注册成功",user.getName());
        return R.success("注册成功");
    }

    @PutMapping("/update") // 更新user
    public R<String> UserInfoChange(@RequestBody User user,HttpSession session){
        // (如果不是本名，改名字了)查数据库有没相同名
        if(session.getAttribute("LoginName")!=user.getName()){
            // 创造筛选条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            // 有这个名字吗
            queryWrapper.eq(User::getName,user.getName());
            // 有相同名就不能更新 更新数据库也会报错
            if(usermapper.selectOne(queryWrapper)!=null)return R.error("名字已存在");
        }


        // 创建时间和id不能改
        User db_user = usermapper.selectById(session.getAttribute("IsLogin").toString());
        if(db_user.getCreate_time()!=user.getCreate_time())return R.error("不能更改创建时间");
        if(db_user.getId()!=user.getId())return R.error("不能更改Id");

        // 防数据和数据库长度不对 抛异常
        try{
            String res = "成功更新"+usermapper.updateById(user)+"行";
            log.info(res);
            return R.success(res);
        }catch (Exception e){
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
            int interval = session.getMaxInactiveInterval();
            log.info("Session超时时间：" + interval + "秒,");
            log.info(name + ": " + value);
            res.add("Session超时时间：" + interval + "秒,"+name + ": " + value);
        }
        return res;
    }
}
