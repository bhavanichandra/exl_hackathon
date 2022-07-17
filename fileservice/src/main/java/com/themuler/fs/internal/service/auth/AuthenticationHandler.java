package com.themuler.fs.internal.service.auth;

import com.themuler.fs.api.Feature;
import com.themuler.fs.internal.model.AppUser;
import com.themuler.fs.internal.service.utility.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class AuthenticationHandler implements AccessInterface {

  private final EncryptionUtils encryptionUtils;

  @Override
  public AppUser loggedInUserData() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return null;
    }
    return (AppUser) authentication.getPrincipal();
  }

  @Override
  public Boolean allowAccess(Feature feature) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return false;
    }
    AppUser user = (AppUser) authentication.getPrincipal();
    return user.getRole().allowedFeatures().contains(feature);
  }
}
