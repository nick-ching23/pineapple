package com.pineapple.veritas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a row/record in the database.
 */
@Setter
@Getter
@TableName("records")
public class Record {
  private String recordId;
  private String orgId;
  private String userId;
  private String flaggedText;
}




