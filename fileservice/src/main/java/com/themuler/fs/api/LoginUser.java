package com.themuler.fs.api;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginUser {
  private String email;
  private String password;
}
