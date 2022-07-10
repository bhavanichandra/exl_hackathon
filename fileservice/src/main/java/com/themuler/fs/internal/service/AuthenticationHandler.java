package com.themuler.fs.internal.service;

import com.themuler.fs.api.Feature;
import com.themuler.fs.api.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHandler implements AccessInterface {

  @Override
  public Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @Override
  public boolean allowAccess(Feature feature) {
    Authentication authentication = this.getAuthentication();
    var principal = (AppUserDetailsService.AppUserDetails) authentication.getPrincipal();
    String currentUserRole = principal.currentUserRole();
    UserRole userRole = UserRole.valueOf(currentUserRole);
    return userRole.allowedFeatures().contains(feature);
  }
}
