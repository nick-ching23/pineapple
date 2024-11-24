package com.pineapple.veritas.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
  private String orgId;
  private String password;
}
