package com.themuler.fs.internal.controller;

import com.themuler.fs.api.DownloadAPIRequest;
import com.themuler.fs.api.Feature;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.internal.gateway.FileServiceMessageGateway;
import com.themuler.fs.internal.model.User;
import com.themuler.fs.internal.service.IntegrationServiceInterface;
import com.themuler.fs.internal.service.auth.AccessInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
public class FileServiceController {

  private final FileServiceMessageGateway fileServiceMessageGateway;

  private final AccessInterface accessInterface;

  @GetMapping(path = "/health")
  public ResponseEntity<String> healthCheck() {
    var message =
        MessageBuilder.withPayload("Hello World").setHeader("operation", "health").build();
    var response = this.fileServiceMessageGateway.send(message);
    return ResponseEntity.ok((String) response);
  }

  @PostMapping(
      path = "/upload",
      consumes = {"multipart/form-data"},
      produces = {"application/json"})
  public ResponseEntity<?> upload(
      @RequestPart(name = "file") MultipartFile filePart, @RequestPart(name = "cloud") String cloud)
      throws IOException {

    boolean allowAccess =
        this.accessInterface.allowAccess(Feature.ANY_FILE_UPLOAD)
            || this.accessInterface.allowAccess(Feature.USER_SPECIFIC_FILE_UPLOAD)
            || this.accessInterface.allowAccess(Feature.CLIENT_SPECIFIC_FILE_UPLOAD);
    if (!allowAccess) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.<List<User>>builder()
                  .payload(null)
                  .success(false)
                  .message("Unauthorized Access")
                  .build());
    }
    String contentType = filePart.getContentType();
    long size = filePart.getSize();
    if (size <= 0) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.<Object>builder()
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
      var message =
          MessageBuilder.withPayload(map)
              .setHeader("operation", "upload")
              .setHeader("cloud", cloud)
              .build();
      var response = this.fileServiceMessageGateway.send(message);
      return ResponseEntity.ok(response);
    } catch (Exception ex) {
      return ResponseEntity.ok("Failed: " + ex.getMessage());
    }
  }

  @PostMapping(
      path = "/download",
      consumes = {"application/json"},
      produces = {"application/json"})
  public ResponseEntity<?> download(@RequestBody DownloadAPIRequest request) {

    boolean allowAccess =
        this.accessInterface.allowAccess(Feature.ANY_FILE_DOWNLOAD)
            || this.accessInterface.allowAccess(Feature.USER_SPECIFIC_FILE_DOWNLOAD)
            || this.accessInterface.allowAccess(Feature.CLIENT_SPECIFIC_FILE_DOWNLOAD);
    if (!allowAccess) {
      return ResponseEntity.status(403)
          .body(
              ResponseWrapper.<List<User>>builder()
                  .payload(null)
                  .success(false)
                  .message("Unauthorized Access")
                  .build());
    }
    try {
      var message = MessageBuilder.withPayload(request).setHeader("operation", "download").build();
      return (ResponseEntity<Object>) this.fileServiceMessageGateway.send(message);
    } catch (Exception ex) {
      ex.printStackTrace();
      return ResponseEntity.ok("Failed: " + ex.getMessage());
    }
  }
}
