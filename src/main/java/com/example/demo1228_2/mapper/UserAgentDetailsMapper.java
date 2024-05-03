package com.example.demo1228_2.mapper;

import com.example.demo1228_2.dto.DailyUniqueVisitorsDto;
import com.example.demo1228_2.entity.UserAgentDetails;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yjz
 * @since 2024-03-19
 */
public interface UserAgentDetailsMapper extends BaseMapper<UserAgentDetails> {
    // 不设置别名 好像会返回null 别名要和Dto一致
    @Select("SELECT Date(create_time) AS create_time, COUNT(DISTINCT user_uuid) AS uuid_count " +
            "FROM t_user_agent_details " +
            "GROUP BY Date(create_time)")
    List<DailyUniqueVisitorsDto> countDailyUniqueVisitors();

    @Select("SELECT DATE(create_time) AS create_time, GROUP_CONCAT(DISTINCT user_uuid) AS user_uuids " +
            "FROM t_user_agent_details " +
            "WHERE create_time >= CURDATE() - INTERVAL 7 DAY " +
            "AND create_time < CURDATE() + INTERVAL 1 DAY " +
            "GROUP BY DATE(create_time)")
    List<Map<String, Object>> countDailyUniqueVisitors2();

    @Select("SELECT user_uuid, COUNT(create_time), " +
            "FROM t_user_agent_details " +
            "WHERE create_time >= #{time} AND create_time < #{time} + INTERVAL 1 DAY " +
            "GROUP BY user_uuid")
    List<Map<String, Object>> countDailyUniqueVisitors3(LocalDate time,String uuid);

    @Select("SELECT user_uuid, COUNT(*) AS total_count, MAX(create_time) AS max_create_time, " +
            "MIN(create_time) AS min_create_time, " +
            "GROUP_CONCAT(DISTINCT visitor_name ORDER BY visitor_name) AS visitor_names, " +
            "GROUP_CONCAT(DISTINCT wechat_nickname ORDER BY wechat_nickname) AS wechat_nickname " +
            "FROM t_user_agent_details " +
            "WHERE create_time >= #{time} AND create_time < #{time} + INTERVAL 1 DAY " +
            "GROUP BY user_uuid")
    List<Map<String, Object>> selectUserAgentSummary(LocalDateTime time);

}
