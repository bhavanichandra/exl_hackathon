package com.themuler.fs.service;

import com.themuler.fs.exception.UserNotFoundException;
import com.themuler.fs.model.ResponseWrapper;
import com.themuler.fs.model.User;
import com.themuler.fs.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserServiceInterface {

  private final AppUserRepository userRepository;

  @Override
  public ResponseWrapper<User> saveUser(User user) {
    var builder = ResponseWrapper.<User>builder();
    try {
      User existingUser = this.userRepository.findByEmail(user.getEmail());
      if (existingUser == null) {
        existingUser = user;
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
}
