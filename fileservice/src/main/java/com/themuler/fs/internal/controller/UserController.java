package com.themuler.fs.internal.controller;

import com.themuler.fs.api.NewUser;
import com.themuler.fs.internal.service.auth.AccessInterface;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.internal.model.User;
import com.themuler.fs.internal.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.themuler.fs.api.Feature.*;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  private final AccessInterface accessInterface;

  @GetMapping(
      path = "/users",
      produces = {"application/json"})
  public ResponseEntity<ResponseWrapper<List<User>>> getUsers() {

    boolean allowAccess = this.accessInterface.allowAccess(GET_ALL_USER);
    if (!allowAccess) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.<List<User>>builder()
                  .payload(null)
                  .success(false)
                  .message("Unauthorized Access")
                  .build());
    }

    var getUsers = this.userService.getAllUsers();
    var status = 200;
    if (!getUsers.getSuccess()) {
      status = 500;
    }
    return ResponseEntity.status(status).body(getUsers);
  }

  @GetMapping(
      path = "/users/{id}",
      consumes = {"application/json"},
      produces = {"application/json"})
  public ResponseEntity<ResponseWrapper<User>> getUsers(@RequestParam long id) {
    boolean allowAccess =
        this.accessInterface.allowAccess(GET_CLIENT_SPECIFIC_USER)
            || this.accessInterface.allowAccess(GET_ALL_USER);
    if (!allowAccess) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.<User>builder()
                  .payload(null)
                  .success(false)
                  .message("Unauthorized Access")
                  .build());
    }
    var getUsersById = this.userService.getUserById(id);
    var status = 200;
    if (!getUsersById.getSuccess()) {
      status = 404;
    }
    return ResponseEntity.status(status).body(getUsersById);
  }

  @PostMapping(
      path = "/users",
      consumes = {"application/json"},
      produces = {"application/json"})
  public ResponseEntity<ResponseWrapper<User>> saveUser(@RequestBody NewUser user) {

    boolean allowAccess =
        this.accessInterface.allowAccess(ADD_ANY_USER)
            || this.accessInterface.allowAccess(UPDATE_ANY_USER)
            || this.accessInterface.allowAccess(ADD_CLIENT_SPECIFIC_USER)
            || this.accessInterface.allowAccess(UPDATE_CLIENT_SPECIFIC_USER);
    if (!allowAccess) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.<User>builder()
                  .payload(null)
                  .success(false)
                  .message("Unauthorized Access")
                  .build());
    }
    var saveUser = this.userService.saveUser(user);
    var status = 201;
    if (!saveUser.getSuccess()) {
      status = 500;
    }
    return ResponseEntity.status(status).body(saveUser);
  }
}
