package com.themuler.fs.internal.controller;

import com.themuler.fs.api.*;
import com.themuler.fs.internal.gateway.FileServiceMessageGateway;
import com.themuler.fs.internal.model.AppUser;
import com.themuler.fs.internal.model.ClientConfiguration;
import com.themuler.fs.internal.model.VirtualFileSystem;
import com.themuler.fs.internal.service.auth.AccessInterface;
import com.themuler.fs.internal.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
public class FileServiceController {

  private final FileServiceMessageGateway fileServiceMessageGateway;

  private final AccessInterface accessInterface;

  private final UserService userService;

  @Value("${environment.active}")
  private String env;

  @PostMapping(
      path = "/upload",
      consumes = {"multipart/form-data"},
      produces = {"application/json"})
  public ResponseEntity<?> upload(
      @RequestPart(name = "file") MultipartFile filePart,
      @RequestPart(name = "cloud", required = false) String cloud)
      throws IOException {

    boolean allowAccess =
        this.accessInterface.allowAccess(Feature.USER_SPECIFIC_FILE_UPLOAD)
            || this.accessInterface.allowAccess(Feature.CLIENT_SPECIFIC_FILE_UPLOAD);
    if (!allowAccess) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.<List<AppUser>>builder()
                  .payload(null)
                  .success(false)
                  .message("Unauthorized Access")
                  .build());
    }
    AppUser loginUser = accessInterface.loggedInUserData();
    String contentType = filePart.getContentType();
    long size = filePart.getSize();
    if (size <= 0) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.builder()
                  .payload(null)
                  .success(false)
                  .message("Please select a file to start uploading")
                  .build());
    }
    String name = filePart.getOriginalFilename();
    InputStream inputStream = filePart.getInputStream();
    var map = new HashMap<String, Object>();
    try {
      map.put("fileContent", inputStream);
      map.put("fileName", name);
      map.put("size", size);
      map.put("contentType", contentType);
      map.put("appUser", loginUser);
      ClientConfiguration clientConfiguration =
          loginUser.getClient().getClientConfigurations().stream()
              .filter(each -> each.getEnvironment().equals(env))
              .findFirst()
              .orElse(null);
      CloudPlatform cloudPlatform = loginUser.getClient().getCloudPlatform();
      if (cloud != null) {
        CloudPlatform[] values = CloudPlatform.values();
        for (CloudPlatform cp : values) {
          if (cp.getCloudPlatform().equals(cloud)) {
            cloudPlatform = cp;
            break;
          }
        }
      }
      map.put("credentials", clientConfiguration);
      var message =
          MessageBuilder.withPayload(map)
              .setHeader("operation", OperationConstants.UPLOAD)
              .setHeader("cloud", cloudPlatform)
              .build();
      var response = this.fileServiceMessageGateway.send(message);
      return ResponseEntity.ok(response);
    } catch (Exception ex) {
      ex.printStackTrace();
      return ResponseEntity.internalServerError()
          .contentType(MediaType.APPLICATION_JSON)
          .body(
              ResponseWrapper.builder()
                  .payload(Map.of("error", ex.getMessage()))
                  .success(false)
                  .message("Error Downloading File")
                  .build());
    }
  }

  @PostMapping(
      path = "/download",
      consumes = {"application/json"},
      produces = {"application/json"})
  public Object download(@RequestBody DownloadAPIRequest request) {

    boolean allowAccess =
        this.accessInterface.allowAccess(Feature.USER_SPECIFIC_FILE_DOWNLOAD)
            || this.accessInterface.allowAccess(Feature.CLIENT_SPECIFIC_FILE_DOWNLOAD)
            || this.accessInterface.allowAccess(Feature.TEMP_DOWNLOAD_ACCESS);
    if (!allowAccess) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.<List<AppUser>>builder()
                  .payload(null)
                  .success(false)
                  .message("Unauthorized Access")
                  .build());
    }
    try {
      AppUser loginUser = accessInterface.loggedInUserData();
      ClientConfiguration clientConfiguration =
          loginUser.getClient().getClientConfigurations().stream()
              .filter(each -> each.getEnvironment().equals(env))
              .findFirst()
              .orElse(null);
      OperationConstants constants = OperationConstants.DOWNLOAD;
      if (loginUser.getRole().equals(UserRole.TEMP_USER)) {
        constants = OperationConstants.TEMP_DOWNLOAD;
      }
      Message<DownloadAPIRequest> message =
          MessageBuilder.withPayload(request)
              .setHeader("operation", constants)
              .setHeader("appUser", loginUser)
              .setHeader("credentials", clientConfiguration)
              .build();
      Object downloadResponse = this.fileServiceMessageGateway.send(message);
      if (downloadResponse instanceof CloudPlatform) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                ResponseWrapper.builder()
                    .payload(Map.of("error", "Not accessible!"))
                    .success(false)
                    .message("Error Downloading File")
                    .build());
      } else {
        return downloadResponse;
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      return ResponseEntity.internalServerError()
          .contentType(MediaType.APPLICATION_JSON)
          .body(
              ResponseWrapper.builder()
                  .payload(Map.of("error", ex.getMessage()))
                  .success(false)
                  .message("Error Downloading File")
                  .build());
    }
  }

  @GetMapping(
      path = "/vfs",
      produces = {"application/json"})
  public ResponseEntity<ResponseWrapper<List<VirtualFileSystem>>> fetchVFSByLoggedInUser(
      @RequestParam(name = "user_id", required = false) String userId) {
    boolean allowAccess = this.accessInterface.allowAccess(Feature.GET_VFS_DATA);
    if (!allowAccess) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.<List<VirtualFileSystem>>builder()
                  .payload(null)
                  .success(false)
                  .message("Unauthorized Access")
                  .build());
    }

    AppUser user = this.accessInterface.loggedInUserData();
    String user_id = user.getId();
    if (userId != null) {
      user_id = userId;
    }
    ResponseWrapper<List<VirtualFileSystem>> uploadedFileDetails =
        this.userService.getUploadedFileDetails(user_id);
    return ResponseEntity.ok().body(uploadedFileDetails);
  }
}
