package com.themuler.fs.internal.service.user;

import com.themuler.fs.api.NewUser;
import com.themuler.fs.internal.repository.ClientRepository;
import com.themuler.fs.internal.repository.RoleRepository;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.internal.model.User;
import com.themuler.fs.internal.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserServiceInterface {

  private final AppUserRepository userRepository;

  private final ClientRepository clientRepository;
  private final RoleRepository roleRepository;

  @Override
  public ResponseWrapper<User> saveUser(NewUser newUser) {
    var builder = ResponseWrapper.<User>builder();
    try {
      User existingUser = this.userRepository.findByEmail(newUser.getEmail());
      if (existingUser == null) {
        existingUser =
            new User(
                null,
                newUser.getName(),
                newUser.getEmail(),
                newUser.getPassword(),
                true,
                clientRepository.findById(newUser.getClientId()).get(),
                roleRepository.getByName(newUser.getRole()));
      }
      User savedUser = this.userRepository.save(existingUser);
      builder.message("User saved / updated successfully");
      builder.success(true);
      builder.payload(savedUser);
    } catch (Exception ex) {
      builder.message("Unexpected User: " + ex.getMessage());
      builder.success(true);
      builder.payload(null);
    }
    return builder.build();
  }

  @Override
  public ResponseWrapper<List<User>> getAllUsers() {
    List<User> users = new ArrayList<>();
    this.userRepository.findAll().forEach(users::add);
    return ResponseWrapper.<List<User>>builder()
        .message("Successfully get users")
        .success(true)
        .payload(users)
        .build();
  }

  @Override
  public ResponseWrapper<User> getUserById(long id) {
    Optional<User> userById = this.userRepository.findById(id);
    if (userById.isEmpty()) {
      return ResponseWrapper.<User>builder()
          .payload(null)
          .success(false)
          .message("User not found")
          .build();
    }
    return ResponseWrapper.<User>builder()
        .message("User retrieved!")
        .success(true)
        .payload(userById.get())
        .build();
  }

  @Override
  public ResponseWrapper<User> validateUser(String username, String password) {
    var user = this.userRepository.findByEmail(username);
    if (user == null) {
      return ResponseWrapper.<User>builder()
          .message("User with username: " + username + " does not exists")
          .success(false)
          .payload(null)
          .build();
    }
    if (!user.getPassword().equals(password)) {
      return ResponseWrapper.<User>builder()
          .message("Invalid password")
          .success(false)
          .payload(null)
          .build();
    }
    return ResponseWrapper.<User>builder()
        .message("Login Successful")
        .success(true)
        .payload(user)
        .build();
  }
}
