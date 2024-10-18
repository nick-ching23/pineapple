package com.pineapple.veritas.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Represents a row/record in the database.
 */
@TableName("records")
public class Record {
  private String orgId;
  private String userId;
  private int numFlags;

  public String getOrgId() { return orgId; }

  public void setOrgId(String orgId) { this.orgId = orgId; }

  public String getUserId() { return userId; }

  public void setUserId(String userId) { this.userId = userId; }

  public int getNumFlags() { return numFlags; }

  public void setNumFlags(int numFlags) { this.numFlags = numFlags; }
}




