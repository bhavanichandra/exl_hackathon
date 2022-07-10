package com.themuler.fs.internal.service;

import com.themuler.fs.api.DownloadAPIRequest;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.internal.model.VirtualFileSystem;
import com.themuler.fs.internal.repository.CloudPlatformRepository;
import com.themuler.fs.internal.repository.VFSRepository;
import com.themuler.fs.internal.service.aws.AwsClient;
import com.themuler.fs.internal.service.azure.AzureConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class IntegrationService implements IntegrationServiceInterface {

  private final AwsClient awsClient;

  private final AzureConnectionFactory azureConnectionFactory;
  private final VFSRepository vfsRepository;
  private final CloudPlatformRepository cloudPlatformRepository;

  @Value("${download.link.active.time}")
  private long durationInMinutes;

  @Override
  public Object uploadToAws(Message<Map<String, Object>> message) {
    VirtualFileSystem vfs = new VirtualFileSystem();
    vfs.setStatus("in_progress");
    var aws = cloudPlatformRepository.findByName("aws").get();
    vfs.setCloudPlatform(aws);
    vfs.setType("File");
    vfs.setParent(null);
    var s3 = awsClient.getS3Client();
    var bucketName = "exl-hackthon";
    Map<String, Object> payload = message.getPayload();
    String fileName = (String) payload.get("fileName");
    vfs.setFileName(fileName);
    long contentLength = (Long) payload.get("size");
    String contentType = (String) payload.get("contentType");
    InputStream inputStream = (FileInputStream) payload.get("fileContent");
    var key = "data/" + fileName;
    PutObjectResponse putObjectResponse =
        s3
            .putObject(
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentLength(contentLength)
                    .contentType(contentType)
                    .build(),
                RequestBody.fromInputStream(inputStream, contentLength))
            .toBuilder()
            .build();
    if (putObjectResponse == null) {
      vfs.setStatus("failed");
      vfsRepository.save(vfs);
      log.info("Failed to upload");
      return ResponseWrapper.<Map<String, Object>>builder()
          .payload(null)
          .success(false)
          .message("Upload Failed");
    }
    vfs.setSavedBucketName(bucketName);
    vfs.setPath(key);
    vfs.setStatus("uploaded");
    VirtualFileSystem savedVFS = vfsRepository.save(vfs);
    log.info("Uploaded to aws");
    return ResponseWrapper.<VirtualFileSystem>builder()
        .payload(savedVFS)
        .success(true)
        .message("Uploaded successfully")
        .build();
  }

  @Override
  public Message<String> getDownloadLocation(DownloadAPIRequest request) {
    VirtualFileSystem vfs = vfsRepository.findByFileNameLikeIgnoreCase(request.getFileName());
    return MessageBuilder.withPayload(vfs.getCloudPlatform().getName())
        .setHeader("path", vfs.getPath())
        .setHeader("fileName", vfs.getFileName())
        .setHeader("bucketName", vfs.getSavedBucketName())
        .setHeader("vfsId", vfs.getId())
        .build();
  }

  @Override
  public Object downloadFromAws(Message<String> message) {
    MessageHeaders headers = message.getHeaders();
    String path = headers.get("path", String.class);
    String bucket = headers.get("bucketName", String.class);
    var s3 = awsClient.getS3Presigner();
    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(path).build();
    GetObjectPresignRequest getObjectPresignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(durationInMinutes))
            .getObjectRequest(getObjectRequest)
            .build();
    PresignedGetObjectRequest presignedGetObjectRequest =
        s3.presignGetObject(getObjectPresignRequest);

    return ResponseWrapper.builder()
        .message("Navigate to open url. Valid for 10 mins or configured time")
        .success(true)
        .payload(Map.of("url", presignedGetObjectRequest.url()))
        .build();
  }

  @Override
  public Object azureCredentials() {
    Map<String, Object> azureCredentials = this.azureConnectionFactory.getAzureCredentials();

    return ResponseWrapper.builder()
        .message("Success")
        .success(true)
        .payload(azureCredentials)
        .build();
  }
}
