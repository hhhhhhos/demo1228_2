package com.example.demo1228_2.mapper;

import com.example.demo1228_2.entity.Test;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yjz
 * @since 2024-01-17
 */
public interface TestMapper extends BaseMapper<Test> {

    // 悲观锁锁定记录 // FOR UPDATE给数据库上行锁 //无@Transactional会立刻释放
    @Select("SELECT * FROM t_test WHERE id = #{id} FOR UPDATE")
    Test selectForUpdate(@Param("id") Long id);

}
