package com.pineapple.veritas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pineapple.veritas.entity.Record;
import org.apache.ibatis.annotations.Mapper;

/**
 * Maps record-related operations to SQL statements.
 */
@Mapper
public interface RecordMapper extends BaseMapper<Record> {

}
