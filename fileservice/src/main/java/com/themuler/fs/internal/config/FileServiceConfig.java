package com.themuler.fs.internal.config;

import com.themuler.fs.internal.repository.UserRepository;
import com.themuler.fs.internal.service.auth.JWTAuthenticationFilter;
import com.themuler.fs.internal.service.auth.JWTServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class FileServiceConfig {

  private final JWTServiceInterface jwtServiceInterface;

  private final UserRepository userRepository;


  @Bean
  public SecurityFilterChain configure(HttpSecurity http) throws Exception {
    http.cors()
        .and()
        .csrf()
        .disable()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        .antMatchers("/public/**", "/actuator/**", "/swagger-ui.html")
        .permitAll()
        .and()
        .authorizeRequests()
        .antMatchers("/api/**", "/admin/**")
        .authenticated();
    http.addFilterBefore(
        new JWTAuthenticationFilter(jwtServiceInterface, userRepository),
        UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
