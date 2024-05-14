package com.example.demo1228_2.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.dto.DailyUniqueVisitorsDto;
import com.example.demo1228_2.entity.*;
import com.example.demo1228_2.mapper.DataResultMapper;
import com.example.demo1228_2.mapper.UserAgentDetailsMapper;
import com.example.demo1228_2.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  查访客记录的
 * </p>
 *
 * @author yjz
 * @since 2024-03-19
 */
@Slf4j
@RestController
@RequestMapping("/user-agent-details")
public class UserAgentDetailsController {

    @Autowired
    UserAgentDetailsMapper userAgentDetailsMapper;

    @GetMapping("/selectbyadmin") // 分页查询 接收params //防空设默认
    public R<Page<UserAgentDetails>> FindPageUsers(@RequestParam Map<String, String> params, HttpSession session){
        // 使用LambdaQueryChainWrapper构建查询
        LambdaQueryChainWrapper<UserAgentDetails> query = new LambdaQueryChainWrapper<>(userAgentDetailsMapper);

        /*
          "ip": null,
          "ip_location":null,
          "url":null,
          "method":null,
          "device":null,
          "brand":null,
          "brand_version":null,
          "os":null,
          "os_version":null,
          "browser":null,
          "uuid":null,
        */


        // 根据条件动态添加查询条件
        if (params.get("ip") != null) {
            query.like(UserAgentDetails::getRealIp, params.get("ip"));
        }
        if (params.get("ip_location") != null) {
            query.like(UserAgentDetails::getCity, params.get("ip_location"));
        }
        if (params.get("url") != null) {
            query.like(UserAgentDetails::getOriginalURI, params.get("url"));
        }
        if (params.get("method") != null) {
            query.like(UserAgentDetails::getMethod, params.get("method"));
        }
        if (params.get("device") != null) {
            query.like(UserAgentDetails::getDeviceName, params.get("device"));
        }
        if (params.get("brand") != null) {
            query.like(UserAgentDetails::getDeviceBrand, params.get("brand"));
        }
        if (params.get("brand_version") != null) {
            query.like(UserAgentDetails::getDeviceVersion, params.get("brand_version"));
        }
        if (params.get("os") != null) {
            query.like(UserAgentDetails::getOperatingSystemName, params.get("os"));
        }
        if (params.get("os_version") != null) {
            query.like(UserAgentDetails::getOperatingSystemVersion, params.get("os_version"));
        }
        if (params.get("browser") != null) {
            query.like(UserAgentDetails::getAgentName, params.get("browser"));
        }
        if (params.get("uuid") != null) {
            query.like(UserAgentDetails::getUser_uuid, params.get("uuid"));
        }
        if (params.get("visitor_name") != null) {
            if(params.get("visitor_name").equals("未登录"))
                query.isNull(UserAgentDetails::getVisitor_name);
            else
                query.like(UserAgentDetails::getVisitor_name, params.get("visitor_name"));
        }
        if (params.get("wechat_nickname") != null) {
            query.like(UserAgentDetails::getWechat_nickname, params.get("wechat_nickname"));
        }


        // 单独处理startDate，如果存在则查询大于等于这个日期的记录
        if (params.get("startDate") != null) {
            query.ge(UserAgentDetails::getCreate_time, params.get("startDate")); // ge是“greater than or equal to”的缩写
        }
        // 单独处理endDate，如果存在则查询小于等于这个日期的记录
        if (params.get("endDate") != null) {
            query.le(UserAgentDetails::getCreate_time, params.get("endDate")); // le是“less than or equal to”的缩写
        }


        Page<UserAgentDetails> page = new Page<>(1,10);
        // 防空参数
        if(params.get("currentPage")!=null && params.get("PageSize")!=null)
            page = new Page<>(Long.parseLong(params.get("currentPage")),Long.parseLong(params.get("PageSize")));

        // 按日期排序
        query.orderByDesc(UserAgentDetails::getCreate_time);

        // 执行分页查询
        Page<UserAgentDetails> result = query.orderByDesc(UserAgentDetails::getCreate_time).page(page);

        // map返回筛选
        R<Page<UserAgentDetails>> response = R.success(result);
        response.setMap(params);

        return response;
    }

    @GetMapping("/select_dashboard_newVisitis") // 查dashboard参数p1
    public List<Map<String, Object>> Dashboard(){
        /*
        LocalDateTime startDate = LocalDate.now().atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(7);


        return new LambdaQueryChainWrapper<>(userAgentDetailsMapper)
                .select(UserAgentDetails::getUser_uuid) // 选择 UUID 字段
                .groupBy(UserAgentDetails::getUser_uuid, // 按 UUID 和日期分组
                        UserAgentDetails::getCreate_time)
                .between(UserAgentDetails::getCreate_time, startDate, endDate) // 时间范围
                .list()
                .stream()
                .filter(record -> record.getCreate_time() != null) // 添加过滤，排除 create_time 为 null 的记录
                .collect(Collectors.groupingBy(
                        record -> record.getCreate_time().toLocalDate(),
                        Collectors.counting() // 计数每天的不重复 UUID
                ))
                .entrySet()
                .stream()
                .map(entry -> new DailyUniqueVisitorsDto(entry.getKey(), entry.getValue().intValue()))
                .collect(Collectors.toList());
                */

        List<Map<String,Object>> rawResults = userAgentDetailsMapper.countDailyUniqueVisitors3();
        log.info("{}",rawResults);


        return rawResults.stream().map(entry -> {
            Map<String, Object> processedEntry = new HashMap<>(entry);
            String uuids = entry.get("user_uuids").toString();
            List<String> uuidList = new ArrayList<>();
            if (!uuids.isEmpty()) {  // 只检查字符串是否非空
                uuidList = Arrays.asList(uuids.split(","));
            }
            processedEntry.put("user_uuids", uuidList);
            return processedEntry;
        }).collect(Collectors.toList());
    }

    @GetMapping("/select_dashboard_purchases") // 查dashboard参数p3
    public List<Map<String, Object>> Dashboard33(){
        return userAgentDetailsMapper.countOrder();
    }

    @GetMapping("/select_dashboard_shoppings") // 查dashboard参数p3
    public List Dashboard333(){
        return userAgentDetailsMapper.countBuylist();
    }

    @PostMapping("/select_dashboard_newVisitis/click") // 查dashboard参数p1
    public Page Dashboard2(@RequestBody Map<String,Object> params){
        // 解析日期字符串
        LocalDate date = LocalDate.parse((String)params.get("create_time"));
        // 转换为LocalDateTime，默认时间为午夜0点
        LocalDateTime create_time = date.atStartOfDay();
        Integer currentPage = (Integer)params.get("currentPage");
        log.info("{},{}",create_time,currentPage);
        Page<Map<String, Object>> page = new Page<>(currentPage, 5);
        /*
        List<UserAgentDetails> re = Db.lambdaQuery(UserAgentDetails.class)
                .between(UserAgentDetails::getCreate_time,create_time,create_time.plusDays(1))
                .list();

        // 按 user_uuid 分组
        Map<String, List<UserAgentDetails>> groupedByUuid = re.stream()
                .collect(Collectors.groupingBy(UserAgentDetails::getUser_uuid));
        */

        return userAgentDetailsMapper.selectUserAgentSummary(page,create_time);
    }

    @Autowired
    UserMapper userMapper;

    @PostMapping("/select_dashboard_purchases/click")
    public IPage Dashboard23(@RequestBody Map<String, Object> params) {
        LocalDate date = LocalDate.parse((String) params.get("create_time"));
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.plusDays(1).atStartOfDay();

        Integer currentPage = (Integer)params.get("currentPage");
        IPage page = new Page<>(currentPage, 5);

        page = Db.lambdaQuery(Order.class)
                .between(Order::getCreate_time, startTime, endTime)
                .orderByDesc(Order::getCreate_time)
                .page(page);
        List<Order> Record = page.getRecords();

        // 聚合用户信息
        ObjectMapper mapper = new ObjectMapper();
        page.setRecords(Record.stream()
                .map(order -> {
                    Long Uid = order.getUser_id();
                    LocalDateTime ct = order.getCreate_time();
                    Map<String,Object> processedEntry = mapper.convertValue(order,Map.class);
                    processedEntry.put("create_time",ct); //时间映射不正确 手动映射
                    processedEntry.put("user_info",userMapper.selectById(Uid));
                    return processedEntry;
                }).collect(Collectors.toList())
        );
        return page;
    }

    @PostMapping("/select_dashboard_shoppings/click")
    public IPage<Buylist> Dashboard253(@RequestBody Map<String, Object> params) {
        LocalDate date = LocalDate.parse((String) params.get("create_time"));
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.plusDays(1).atStartOfDay();

        Integer currentPage = (Integer)params.get("currentPage");
        IPage<Buylist> page = new Page<>(currentPage, 5);

        return Db.lambdaQuery(Buylist.class)
                .between(Buylist::getCreate_time, startTime, endTime)
                .orderByDesc(Buylist::getCreate_time)
                .page(page);
    }




    @Autowired
    DataResultMapper dataResultMapper;

    @GetMapping("/init_dashboard_four")
    public Map Dashboard3(){
        Map<String,Object> params = new HashMap<>();
        //params.put("p1",userAgentDetailsMapper.countDistinctUserUuid());
        params.put("p1", dataResultMapper.selectById(45698).getHome_visitors());
        params.put("p3",Db.lambdaQuery(Order.class).count());
        params.put("p4",Db.lambdaQuery(Buylist.class).count());
        return params;
    }

}
