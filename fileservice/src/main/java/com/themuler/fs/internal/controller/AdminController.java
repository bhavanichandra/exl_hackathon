package com.themuler.fs.internal.controller;

import com.themuler.fs.api.NewClient;
import com.themuler.fs.api.NewClientConfiguration;
import com.themuler.fs.api.NewUser;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.internal.model.AppUser;
import com.themuler.fs.internal.model.Client;
import com.themuler.fs.internal.service.auth.AccessInterface;
import com.themuler.fs.internal.service.user.AdminInterface;
import com.themuler.fs.internal.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.themuler.fs.api.Feature.*;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
public class AdminController {

  private final UserService userService;

  private final AccessInterface accessInterface;

  private final AdminInterface adminInterface;

  @GetMapping(
      path = "/users",
      produces = {"application/json"})
  @ResponseBody
  public ResponseEntity<ResponseWrapper<List<AppUser>>> getUsers() {

    boolean allowAccess = this.accessInterface.allowAccess(GET_ALL_USER);
    if (!allowAccess) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.<List<AppUser>>builder()
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
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(getUsers);
  }

  @GetMapping(
      path = "/users/{id}",
      consumes = {"application/json"},
      produces = {"application/json"})
  @ResponseBody
  public ResponseEntity<ResponseWrapper<AppUser>> getUsers(@PathVariable("id") String id) {
    boolean allowAccess =
        this.accessInterface.allowAccess(GET_CLIENT_SPECIFIC_USER)
            || this.accessInterface.allowAccess(GET_ALL_USER);
    if (!allowAccess) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.<AppUser>builder()
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
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(getUsersById);
  }

  @PostMapping(
      path = "/users",
      consumes = {"application/json"},
      produces = {"application/json"})
  @ResponseBody
  public ResponseEntity<ResponseWrapper<AppUser>> saveUser(@RequestBody NewUser user) {

    boolean allowAccess =
        this.accessInterface.allowAccess(ADD_ANY_USER)
            || this.accessInterface.allowAccess(UPDATE_ANY_USER)
            || this.accessInterface.allowAccess(ADD_CLIENT_SPECIFIC_USER)
            || this.accessInterface.allowAccess(UPDATE_CLIENT_SPECIFIC_USER);
    if (!allowAccess) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.<AppUser>builder()
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
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(saveUser);
  }

  @PostMapping(
      value = "/clients",
      produces = {"application/json"},
      consumes = {"application/json"})
  public ResponseEntity<ResponseWrapper<Client>> saveClient(@RequestBody  NewClient client) {
    boolean allowAccess = this.accessInterface.allowAccess(ADD_NEW_CLIENTS);
    ResponseWrapper.ResponseWrapperBuilder<Client> builder = ResponseWrapper.builder();
    if (!allowAccess) {
      return ResponseEntity.status(403)
          .body(builder.payload(null).success(false).message("Unauthorized Access").build());
    }
    ResponseWrapper<Client> clientResponseWrapper = this.adminInterface.addClient(client);
    if (clientResponseWrapper.getSuccess()) {
      return ResponseEntity.ok(clientResponseWrapper);
    } else {
      return ResponseEntity.internalServerError().body(clientResponseWrapper);
    }
  }

  @PostMapping(
      value = "/clients/{client_id}/config",
      produces = {"application/json"},
      consumes = {"application/json"})
  public ResponseEntity<ResponseWrapper<Client>> saveClient(
      @PathVariable(value = "client_id") String clientId, @RequestBody NewClientConfiguration clientConfig) {
    boolean allowAccess = this.accessInterface.allowAccess(ADD_NEW_CONFIGURATIONS);
    if (!allowAccess) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.<Client>builder()
                  .payload(null)
                  .success(false)
                  .message("Unauthorized Access")
                  .build());
    }

    ResponseWrapper<Client> clientResponseWrapper =
        this.adminInterface.addClientConfiguration(clientId, clientConfig);
    if (clientResponseWrapper.getSuccess()) {
      return ResponseEntity.ok(clientResponseWrapper);
    } else {
      return ResponseEntity.internalServerError().body(clientResponseWrapper);
    }
  }
}
