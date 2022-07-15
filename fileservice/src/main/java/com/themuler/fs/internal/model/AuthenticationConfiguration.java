package com.themuler.fs.internal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AuthenticationConfiguration {

  private String cloudName;
  private Map<String, Object> credentials;
}
