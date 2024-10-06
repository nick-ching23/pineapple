package com.pineapple.veritas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("records")
public class Record {
  private String orgID;
  private String userID;
  private int numFlags;
}
