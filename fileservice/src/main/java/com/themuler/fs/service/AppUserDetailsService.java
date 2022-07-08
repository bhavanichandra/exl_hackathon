package com.themuler.fs.service;

import com.themuler.fs.model.User;
import com.themuler.fs.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

  private AppUserRepository appUserRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = this.appUserRepository.findByEmail(username);
    if (user == null) {
      throw new UsernameNotFoundException("User with username: " + username + " does not exists.");
    }
    return new AppUserDetails(user);
  }

  static class AppUserDetails extends User implements UserDetails {

    private static final List<GrantedAuthority> USER_ROLE =
        Collections.unmodifiableList(
            AuthorityUtils.createAuthorityList("SUPER_ADMIN", "CLIENT_ADMIN", "USER", "TEMP_USER"));

    public AppUserDetails(User user) {
      super(
          user.getId(),
          user.getName(),
          user.getEmail(),
          user.getPassword(),
          user.getIs_active(),
          user.getClient(),
          user.getRole());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return null;
    }

    @Override
    public String getUsername() {
      return getEmail();
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
  }
}
