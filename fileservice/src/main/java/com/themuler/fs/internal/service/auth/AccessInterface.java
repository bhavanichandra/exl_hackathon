package com.themuler.fs.internal.service.auth;

import com.themuler.fs.api.Feature;
import com.themuler.fs.internal.model.AuthenticationData;
import org.springframework.security.core.Authentication;

import java.util.Map;

public interface AccessInterface {

  Authentication getAuthentication();

  boolean allowAccess(Feature feature);

  AuthenticationData authenticationInformation();
}
