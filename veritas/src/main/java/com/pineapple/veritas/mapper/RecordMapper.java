package com.pineapple.veritas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pineapple.veritas.entity.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * Maps record-related operations to SQL statements.
 */
@Mapper
public interface RecordMapper extends BaseMapper<Record> {

  @Update("UPDATE records SET numFlags = #{numFlags} WHERE orgID = #{orgId} AND userID = #{userId}")
  int updateByCompositeKey(@Param("userId") String userId,
                           @Param("orgId") String orgId,
                           @Param("numFlags") int numFlags);
}
