package com.themuler.fs.api;

import com.themuler.fs.internal.model.AppUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class LoginResponse {
  private String token;
  private AppUser user;
  private List<String> permissions;
}
