package com.themuler.fs.controller;

import com.themuler.fs.model.ResponseWrapper;
import com.themuler.fs.model.User;
import com.themuler.fs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping(
      path = "/users",
      produces = {"application/json"})
  public ResponseEntity<ResponseWrapper<List<User>>> getUsers() {
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
  public ResponseEntity<ResponseWrapper<User>> saveUser(@RequestBody User user) {
    var saveUser = this.userService.saveUser(user);
    var status = 201;
    if (!saveUser.getSuccess()) {
      status = 500;
    }
    return ResponseEntity.status(status).body(saveUser);
  }
}
