package com.themuler.fs.controller;

import com.themuler.fs.gateway.FileServiceMessageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FileServiceController {

  private final FileServiceMessageGateway fileServiceMessageGateway;

  @GetMapping(path = "/health")
  public ResponseEntity<String> healthCheck() {
    var message = MessageBuilder.withPayload("Hello World")
            .setHeader("operation", "ping")
            .build();
    var response = this.fileServiceMessageGateway.send(message);
    return ResponseEntity.ok((String) response);
  }

  @PostMapping(path = "/upload", consumes = {"multipart/form-data"})
  public ResponseEntity<?> uploadToAws(@RequestPart MultipartFile file) throws IOException {
    var map = Map.of("fileName", file.getName(), "fileContent", file.getInputStream());
    var message = MessageBuilder.withPayload(map)
            .setHeader("operation", "upload")
            .setHeader("cloud", "aws")
            .build();
    var response = this.fileServiceMessageGateway.send(message);
    return ResponseEntity.ok(response);
  }
}
