package com.themuler.fs.internal.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.themuler.fs.internal.model.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtService implements JWTServiceInterface {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Override
  public Boolean validateJWTToken(String token) {
    Optional<DecodedJWT> decodedJWT = decodeToken(token);
    return decodedJWT.isPresent();
  }

  @Override
  public String createToken(AppUser user) {
    return JWT.create()
        .withClaim("userId", user.getId().toString())
        .withIssuedAt(Instant.now())
        .withExpiresAt(new Date(new Date().getTime() + 60 * 60 * 1000))
        .withSubject(user.getEmail())
        .sign(Algorithm.HMAC256(jwtSecret));
  }

  @Override
  public Optional<String> getSubjectFromToken(String token) {
    Optional<DecodedJWT> decodedJWT = decodeToken(token);
    if (decodedJWT.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(decodedJWT.get().getSubject());
  }

  private Optional<DecodedJWT> decodeToken(String token) {
    try {
      JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
      DecodedJWT jwt = verifier.verify(token);
      return Optional.of(jwt);
    } catch (JWTVerificationException ex) {
      return Optional.empty();
    }
  }
}
