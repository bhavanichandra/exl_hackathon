package com.themuler.fs.internal.config;

import com.themuler.fs.internal.service.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class FileServiceConfig {

  @Bean
  public PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    return httpSecurity
        .cors()
        .and()
        .csrf()
        .disable()
        .sessionManagement(sc -> sc.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeRequests(auth -> auth.mvcMatchers("/api/**", "/admin/**").authenticated())
        .authorizeRequests(skip -> skip.mvcMatchers("/public/**").permitAll())
//            .addFilterBefore(new JwtAuthenticationFilter())
        .build();
  }
}
