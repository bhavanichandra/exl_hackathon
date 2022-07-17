package com.themuler.fs.internal.service.auth;

import com.themuler.fs.api.Feature;
import com.themuler.fs.internal.model.AuthenticationData;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHandler implements AccessInterface {
  @Override
  public Boolean allowAccess(Feature feature) {
    return true;
  }

  @Override
  public AuthenticationData getClientConfiguration(String environment) {
    return null;
  }
}
