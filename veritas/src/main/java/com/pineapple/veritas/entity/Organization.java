package com.pineapple.veritas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a row/record in the database.
 */
@Setter
@Getter
@TableName("organizations")
public class Organization {
  private String orgId;
  private String password;
}