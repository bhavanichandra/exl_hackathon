package com.themuler.fs.internal.controller;

import com.themuler.fs.api.LoginResponse;
import com.themuler.fs.api.LoginUser;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.internal.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/public")
@RequiredArgsConstructor
public class LoginController {

  private final UserService userService;

  @PostMapping(path = "/login")
  @ResponseBody
  public ResponseEntity<ResponseWrapper<LoginResponse>> loginUser(
      @RequestBody LoginUser loginUser) {
    var loginResponse =
        this.userService.validateUser(loginUser.getEmail(), loginUser.getPassword());
    if (!loginResponse.getSuccess()) {
      return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(loginResponse);
    }
    return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(loginResponse);
  }
}
