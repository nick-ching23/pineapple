package com.pineapple.veritas.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Represents a row/record in the database.
 */
@Data
@TableName("records")
public class Record {
  private String orgId;
  private String userId;
  private int numFlags;
}
