package com.themuler.fs.internal.service.auth;

import com.themuler.fs.api.Feature;
import com.themuler.fs.internal.model.AppUser;
import com.themuler.fs.internal.model.AuthenticationData;

public interface AccessInterface {
  Boolean allowAccess(Feature feature);

  AppUser loggedInUserData();
}
