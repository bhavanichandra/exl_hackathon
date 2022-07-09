package com.themuler.fs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class FileServiceConfig {

  @Bean
  public PasswordEncoder encoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    return httpSecurity
        .csrf()
        .disable()
        .authorizeRequests(
            auth -> {
              try {
                auth.mvcMatchers("/api/**")
                    .authenticated()
                    .and()
                    .authorizeRequests(skip -> skip.mvcMatchers("/login", "/register").permitAll());
              } catch (Exception e) {
                e.printStackTrace();
              }
            })
        .httpBasic(Customizer.withDefaults())
        .build();
  }
}
