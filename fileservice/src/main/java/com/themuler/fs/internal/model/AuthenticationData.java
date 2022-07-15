package com.themuler.fs.internal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AuthenticationData {

  private String userId;
  private String clientId;
  private List<AuthenticationConfiguration> configurations;

}
