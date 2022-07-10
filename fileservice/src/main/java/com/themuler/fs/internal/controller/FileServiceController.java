package com.themuler.fs.internal.controller;

import com.themuler.fs.internal.gateway.FileServiceMessageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
public class FileServiceController {

  private final FileServiceMessageGateway fileServiceMessageGateway;

  @GetMapping(path = "/health")
  public ResponseEntity<String> healthCheck() {
    var message =
        MessageBuilder.withPayload("Hello World").setHeader("operation", "health").build();
    var response = this.fileServiceMessageGateway.send(message);
    return ResponseEntity.ok((String) response);
  }

  @PostMapping(
      path = "/upload",
      consumes = {"multipart/form-data"})
  public ResponseEntity<?> uploadToAws(@RequestPart(name = "file") MultipartFile filePart)
      throws IOException {
    String contentType = filePart.getContentType();
    long size = filePart.getSize();
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
              .setHeader("cloud", "aws")
              .build();
      var response = this.fileServiceMessageGateway.send(message);
      return ResponseEntity.ok(response);
    } catch (Exception ex) {
      return ResponseEntity.ok("Failed: " + ex.getMessage());
    }
  }
}
