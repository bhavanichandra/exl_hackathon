package com.themuler.fs.internal.service.auth;

import com.themuler.fs.api.Feature;
import com.themuler.fs.internal.model.AuthenticationData;

public interface AccessInterface {
  Boolean allowAccess(Feature feature);

  AuthenticationData getClientConfiguration(String environment);
}
