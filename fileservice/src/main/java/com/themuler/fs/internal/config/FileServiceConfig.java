package com.themuler.fs.internal.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
public class FileServiceConfig {

  @Bean
  public PasswordEncoder encoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    return httpSecurity
        .sessionManagement(sc -> sc.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf()
        .disable()
        .authorizeRequests(auth -> auth.mvcMatchers("/api/**", "/admin/**").authenticated())
        .authorizeRequests(skip -> skip.mvcMatchers("/public/**").permitAll())
        .httpBasic(Customizer.withDefaults())
        .build();
  }
}
