package com.pineapple.veritas.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Contains the organisation ID and password for a login request.
 */
@Getter
@Setter
public class LoginRequest {
  private String orgId;
  private String password;
}
