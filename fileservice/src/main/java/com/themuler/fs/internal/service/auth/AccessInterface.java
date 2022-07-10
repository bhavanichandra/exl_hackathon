package com.themuler.fs.internal.service.auth;

import com.themuler.fs.api.Feature;
import org.springframework.security.core.Authentication;

public interface AccessInterface {

  Authentication getAuthentication();

  boolean allowAccess(Feature feature);
}