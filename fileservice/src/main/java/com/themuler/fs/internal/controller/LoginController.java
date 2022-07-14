package com.themuler.fs.internal.controller;

import com.themuler.fs.api.LoginUser;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.internal.model.User;
import com.themuler.fs.internal.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/public")
@RequiredArgsConstructor
public class LoginController {

  private final UserService userService;

  @PostMapping(path = "/login")
  public ResponseEntity<ResponseWrapper<User>> loginUser(@RequestBody LoginUser loginUser) {
    var loginResponse =
        this.userService.validateUser(loginUser.getEmail(), loginUser.getPassword());
    if (!loginResponse.getSuccess()) {
      return ResponseEntity.status(401).body(loginResponse);
    }
    return ResponseEntity.status(200).body(loginResponse);
  }
}
