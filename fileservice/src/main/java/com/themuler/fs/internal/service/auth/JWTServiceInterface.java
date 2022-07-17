package com.themuler.fs.internal.service.auth;

import com.themuler.fs.internal.model.AppUser;

import java.util.Optional;

public interface JWTServiceInterface {

  Boolean validateJWTToken(String token);

  String createToken(AppUser user);

  Optional<String> getSubjectFromToken(String token);

}
