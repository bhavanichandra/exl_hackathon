package com.themuler.fs.internal.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuler.fs.api.UserRole;
import com.themuler.fs.internal.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  @Value("${jwt.secret}")
  private String jwtSecret;

  public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;

    setFilterProcessesUrl("/api/services/controller/user/login");
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    try {
      User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
      List<SimpleGrantedAuthority> userRoles =
          Arrays.stream(UserRole.values())
              .map(Enum::toString)
              .map(m -> new SimpleGrantedAuthority("ROLE_" + m))
              .collect(Collectors.toUnmodifiableList());
      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword(), userRoles));
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authResult)
      throws IOException {
    User principal = (User) authResult.getPrincipal();
    String token =
        JWT.create()
            .withSubject(principal.getEmail())
            .withExpiresAt(new Date(System.currentTimeMillis() + 900_000))
            .sign(Algorithm.HMAC512(jwtSecret.getBytes()));
    String body = ((User) authResult.getPrincipal()).getEmail() + " " + token;
    response.getWriter().write(body);
    response.getWriter().flush();
  }
}
