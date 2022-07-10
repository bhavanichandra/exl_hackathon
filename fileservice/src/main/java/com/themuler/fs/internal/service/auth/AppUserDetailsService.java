package com.themuler.fs.internal.service.auth;

import com.themuler.fs.api.UserRole;
import com.themuler.fs.internal.model.User;
import com.themuler.fs.internal.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class AppUserDetailsService implements UserDetailsService {

  private final AppUserRepository appUserRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = this.appUserRepository.findByEmail(username);
    if (user == null) {
      throw new UsernameNotFoundException("User with username: " + username + " does not exists.");
    }

    return new AppUserDetails(user, appUserRepository);
  }

  @RequiredArgsConstructor
  public static class AppUserDetails implements UserDetails {

    private final User user;

    private final AppUserRepository repo;

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
      return Arrays.stream(UserRole.values())
          .map(Enum::toString)
          .map(m -> new SimpleGrantedAuthority("ROLE_" + m))
          .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public String getPassword() {
      return user.getPassword();
    }

    @Override
    public String getUsername() {
      return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
      return true;
    }

    @Override
    public boolean isAccountNonLocked() {
      return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
      return true;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }

    public String currentUserRole() {
      return user.getRole().getName();
    }

    public User getUser() {
      return user;
    }
  }
}
