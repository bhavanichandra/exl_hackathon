package com.themuler.fs.internal.service.user;

import com.themuler.fs.api.LoginResponse;
import com.themuler.fs.api.NewUser;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.api.UserRole;
import com.themuler.fs.internal.model.AppUser;
import com.themuler.fs.internal.model.Client;
import com.themuler.fs.internal.repository.ClientRepository;
import com.themuler.fs.internal.repository.UserRepository;
import com.themuler.fs.internal.service.auth.JWTServiceInterface;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserServiceInterface {

  private final UserRepository userRepository;

  private final ClientRepository clientRepository;

  private final JWTServiceInterface jwtService;

  @Override
  public ResponseWrapper<AppUser> saveUser(NewUser newUser) {
    var builder = ResponseWrapper.<AppUser>builder();
    try {
      AppUser user = this.userRepository.findByEmail(newUser.getEmail());
      if (user == null) {
        Client client = clientRepository.findByName(newUser.getClient());
        user =
            new AppUser(
                null,
                newUser.getEmail(),
                newUser.getPassword(),
                newUser.getName(),
                client,
                UserRole.CLIENT_ADMIN);
      }
      AppUser savedUser = this.userRepository.insert(user);
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
  public ResponseWrapper<List<AppUser>> getAllUsers() {
    List<AppUser> users = new ArrayList<>();
    this.userRepository.findAll().forEach(users::add);
    return ResponseWrapper.<List<AppUser>>builder()
        .message("Successfully get users")
        .success(true)
        .payload(users)
        .build();
  }

  @Override
  public ResponseWrapper<AppUser> getUserById(String id) {
    Optional<AppUser> userById = this.userRepository.findById(new ObjectId(id));
    if (userById.isEmpty()) {
      return ResponseWrapper.<AppUser>builder()
          .payload(null)
          .success(false)
          .message("User not found")
          .build();
    }
    return ResponseWrapper.<AppUser>builder()
        .message("User retrieved!")
        .success(true)
        .payload(userById.get())
        .build();
  }

  @Override
  public ResponseWrapper<LoginResponse> validateUser(String username, String password) {
    var user = this.userRepository.findByEmail(username);
    if (user == null) {
      return ResponseWrapper.<LoginResponse>builder()
          .message("User with username: " + username + " does not exists")
          .success(false)
          .payload(null)
          .build();
    }
    if (!user.getPassword().equals(password)) {
      return ResponseWrapper.<LoginResponse>builder()
          .message("Invalid password")
          .success(false)
          .payload(null)
          .build();
    }

    String token = jwtService.createToken(user);
    LoginResponse loginResponse =
        LoginResponse.builder()
            .token(token)
            .user(user)
            .permissions(
                UserRole.valueOf(user.getName()).allowedFeatures().stream()
                    .map(Enum::toString)
                    .collect(Collectors.toList()))
            .build();
    return ResponseWrapper.<LoginResponse>builder()
        .message("Login Successful")
        .success(true)
        .payload(loginResponse)
        .build();
  }
}
