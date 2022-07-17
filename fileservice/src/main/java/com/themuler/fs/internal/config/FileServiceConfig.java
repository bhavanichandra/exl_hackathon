package com.themuler.fs.internal.config;

import com.themuler.fs.internal.repository.UserRepository;
import com.themuler.fs.internal.service.auth.JWTAuthenticationFilter;
import com.themuler.fs.internal.service.auth.JWTServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableWebMvc
public class FileServiceConfig implements WebMvcConfigurer {

  private final JWTServiceInterface jwtServiceInterface;
  private final UserRepository userRepository;
  @Value("${temp.location}")
  private String tempLocation;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/files/**").addResourceLocations("file:" + tempLocation);
  }

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
        .antMatchers("/public/**", "/actuator/**", "/swagger-ui.html", "/files/**")
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
