package com.example.demo1228_2.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    @Select("SELECT date_series.date AS create_time, COALESCE(GROUP_CONCAT(DISTINCT tua.user_uuid), '') AS user_uuids " +
            "FROM ( " +
            "    SELECT CURDATE() + INTERVAL a - 7 DAY AS date " +
            "    FROM ( " +
            "        SELECT 1 AS a UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 " +
            "    ) AS a " +
            ") AS date_series " +
            "LEFT JOIN t_user_agent_details tua " +
            "ON DATE(tua.create_time) = date_series.date " +
            "AND tua.create_time >= CURDATE() - INTERVAL 7 DAY " +
            "AND tua.create_time < CURDATE() + INTERVAL 1 DAY " +
            "GROUP BY date_series.date " +
            "ORDER BY date_series.date")
    List<Map<String, Object>> countDailyUniqueVisitors3();

    @Select("SELECT date_series.date AS create_time, COALESCE(COUNT(t_order.id), 0) AS count " +
            "FROM ( " +
            "    SELECT CURDATE() + INTERVAL a - 7 DAY AS date " +
            "    FROM ( " +
            "        SELECT 1 AS a UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 " +
            "    ) AS a " +
            ") AS date_series " +
            "LEFT JOIN t_order ON DATE(t_order.create_time) = date_series.date " +
            "GROUP BY date_series.date " +
            "ORDER BY date_series.date")
    List<Map<String, Object>> countOrder();

    @Select("SELECT date_series.date AS create_time, COALESCE(COUNT(t_buylist.id), 0) AS count " +
            "FROM ( " +
            "    SELECT CURDATE() + INTERVAL a - 7 DAY AS date " +
            "    FROM ( " +
            "        SELECT 1 AS a UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 " +
            "    ) AS a " +
            ") AS date_series " +
            "LEFT JOIN t_buylist ON DATE(t_buylist.create_time) = date_series.date " +
            "GROUP BY date_series.date " +
            "ORDER BY date_series.date")
    List<Map<String, Object>> countBuylist();






    // dashboard 最下方table的数据
    @Select("SELECT user_uuid, COUNT(*) AS total_count, " +
            "DATE_FORMAT(MAX(create_time), '%Y-%m-%d %H:%i:%s') AS max_create_time, " +
            "DATE_FORMAT(MIN(create_time), '%Y-%m-%d %H:%i:%s') AS min_create_time, " +
            "GROUP_CONCAT(DISTINCT visitor_name ORDER BY visitor_name) AS visitor_names, " +
            "GROUP_CONCAT(DISTINCT city) AS city, " +
            "GROUP_CONCAT(DISTINCT wechat_nickname ORDER BY wechat_nickname) AS wechat_nickname " +
            "FROM t_user_agent_details " +
            "WHERE create_time >= #{time} AND create_time < #{time} + INTERVAL 1 DAY " +
            "GROUP BY user_uuid " +
            "ORDER BY total_count DESC")
    Page<Map<String, Object>> selectUserAgentSummary(Page<?> page, LocalDateTime time);


    // 返回不重复uuid数量
    @Select("SELECT COUNT(DISTINCT user_uuid) FROM t_user_agent_details")
    int countDistinctUserUuid();

}
