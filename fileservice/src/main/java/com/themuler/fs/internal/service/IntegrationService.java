package com.themuler.fs.internal.service;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.storage.StorageScopes;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.themuler.fs.api.CloudPlatform;
import com.themuler.fs.api.DownloadAPIRequest;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.internal.model.AppUser;
import com.themuler.fs.internal.model.ClientConfiguration;
import com.themuler.fs.internal.model.VirtualFileSystem;
import com.themuler.fs.internal.repository.VFSRepository;
import com.themuler.fs.internal.service.utility.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class IntegrationService implements IntegrationServiceInterface {

  private final VFSRepository vfsRepository;

  private final EncryptionUtils encryptionUtils;

  @Value("${download.link.active.time}")
  private long durationInMinutes;

  private S3Client getS3Client(Map<String, String> decryptCredentials) {
    return S3Client.builder()
        .credentialsProvider(
            () ->
                AwsBasicCredentials.create(
                    decryptCredentials.get("client_id"), decryptCredentials.get("client_secret")))
        .build();
  }

  @Override
  public Object uploadToAws(Message<Map<String, Object>> message) {
    VirtualFileSystem vfs = new VirtualFileSystem();
    vfs.setStatus("in_progress");
    var aws = CloudPlatform.AWS.name();
    vfs.setCloudPlatform(aws);
    vfs.setVfsType("File");

    Map<String, Object> payload = message.getPayload();
    AppUser user = new ObjectMapper().convertValue(payload.get("appUser"), AppUser.class);
    ClientConfiguration configuration =
        new ObjectMapper().convertValue(payload.get("credentials"), ClientConfiguration.class);
    assert configuration != null;
    Map<String, String> decryptCredentials = this.decryptCredentials(configuration);
    log.info("Credentials: {}", decryptCredentials);
    var s3 = getS3Client(decryptCredentials);
    var bucketName = decryptCredentials.get("default_bucket_name");
    String fileName = (String) payload.get("fileName");
    vfs.setFileName(fileName);
    vfs.setUser(user);
    vfs.setClient(user.getClient());
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
    vfs.setBucketName(bucketName);
    vfs.setCloudPath(key);
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
  public Object uploadToAzure(Message<Map<String, Object>> message) {
    Map<String, Object> payload = message.getPayload();
    ClientConfiguration configuration =
        new ObjectMapper().convertValue(payload.get("credentials"), ClientConfiguration.class);
    assert configuration != null;
    Map<String, String> decryptCredentials = this.decryptCredentials(configuration);
    log.info("Credentials: {}", decryptCredentials);
    String fileName = (String) payload.get("fileName");
    InputStream fileInputStream = (InputStream) payload.get("fileContent");
    String accountName = decryptCredentials.get("storage_account");
    String default_bucket_name = decryptCredentials.get("default_bucket_name");
    String connection_string = decryptCredentials.get("connection_string");
    String url =
        UriComponentsBuilder.newInstance()
            .scheme("https")
            .host(accountName + ".blob.core.windows.net")
            .path("/{container_name}/{blob_name}")
            .uriVariables(Map.of("container_name", default_bucket_name, "blob_name", fileName))
            .build()
            .toUriString();
    log.info("Url: {}", url);
    VirtualFileSystem vfs = new VirtualFileSystem();
    AppUser user = new ObjectMapper().convertValue(payload.get("appUser"), AppUser.class);
    vfs.setClient(user.getClient());
    vfs.setUser(user);
    vfs.setBucketName(default_bucket_name);
    vfs.setVfsType("File");
    vfs.setCloudPlatform(CloudPlatform.AZURE.name());
    vfs.setStatus("in_progress");
    vfs.setCloudPath(default_bucket_name + "/" + fileName);
    vfs.setFileName(fileName);
    log.info("Credentials: " + decryptCredentials);
    BlobClient blobClient =
        new BlobClientBuilder().endpoint(url).connectionString(connection_string).buildClient();
    var options = new BlobParallelUploadOptions(fileInputStream);
    Response<BlockBlobItem> blockBlobItemResponse =
        blobClient.uploadWithResponse(options, Duration.ofMinutes(10), Context.NONE);
    long statusCode = blockBlobItemResponse.getStatusCode();
    if (statusCode != 200) {
      vfs.setStatus("failed");
      return ResponseWrapper.<VirtualFileSystem>builder()
          .success(false)
          .payload(vfs)
          .message("Upload Failed")
          .build();
    }
    vfs.setStatus("upload successful");
    vfsRepository.save(vfs);
    return ResponseWrapper.<VirtualFileSystem>builder()
        .success(true)
        .payload(vfs)
        .message("Upload successful")
        .build();
  }

  @Override
  public Object uploadToGcp(Message<Map<String, Object>> message) {
    Map<String, Object> payload = message.getPayload();
    ClientConfiguration configuration =
        new ObjectMapper().convertValue(payload.get("credentials"), ClientConfiguration.class);
    assert configuration != null;
    Map<String, String> decryptCredentials = this.decryptCredentials(configuration);
    log.info("Credentials: {}", decryptCredentials);
    String fileName = (String) payload.get("fileName");
    InputStream fileInputStream = (InputStream) payload.get("fileContent");
    String default_bucket_name = decryptCredentials.get("default_bucket_name");
    String contentType = (String) payload.get("contentType");
    VirtualFileSystem vfs = new VirtualFileSystem();
    vfs.setVfsType("File");
    vfs.setBucketName(default_bucket_name);
    vfs.setCloudPlatform(CloudPlatform.GOOGLE_CLOUD_PLATFORM.name());
    vfs.setStatus("in_progress");
    vfs.setCloudPlatform(default_bucket_name + "/" + fileName);
    vfs.setFileName(fileName);
    AppUser user = new ObjectMapper().convertValue(payload.get("appUser"), AppUser.class);
    vfs.setClient(user.getClient());
    vfs.setUser(user);
    try {
      String clientId = decryptCredentials.get("client_id");
      String clientEmail = decryptCredentials.get("client_email");
      String privateKeyId = decryptCredentials.get("private_key_id");
      String privateKey = decryptCredentials.get("private_key");
      ServiceAccountCredentials serviceAccountCredentials =
          ServiceAccountCredentials.fromPkcs8(
              clientId,
              clientEmail,
              privateKey,
              privateKeyId,
              Collections.singletonList(StorageScopes.DEVSTORAGE_FULL_CONTROL));
      Storage storage =
          StorageOptions.newBuilder()
              .setCredentials(serviceAccountCredentials)
              .build()
              .getService();
      BlobId blobId = BlobId.of(default_bucket_name, fileName);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
      final byte[] bytes;
      bytes = fileInputStream.readAllBytes();
      Blob blob = storage.create(blobInfo, bytes);
      if (blob != null) {
        WritableByteChannel channel = blob.writer();
        channel.write(ByteBuffer.wrap(bytes));
        channel.close();
      }
      vfs.setStatus("upload successful");
    } catch (Exception ex) {
      ex.printStackTrace();
      vfs.setStatus("failed");
      return ResponseWrapper.<VirtualFileSystem>builder()
          .payload(vfs)
          .success(false)
          .message("Upload Failed")
          .build();
    }
    vfsRepository.save(vfs);
    return ResponseWrapper.<VirtualFileSystem>builder()
        .payload(vfs)
        .success(true)
        .message("successfully uploaded")
        .build();
  }

  @Override
  public Message<CloudPlatform> getDownloadLocation(DownloadAPIRequest request) {
    VirtualFileSystem vfs = vfsRepository.findByFileNameLikeIgnoreCase(request.getFileName());
    CloudPlatform cloudPlatform =
        Arrays.stream(CloudPlatform.values())
            .filter(each -> each.getCloudPlatform().equals(vfs.getCloudPlatform()))
            .collect(Collectors.toList())
            .get(0);
    return MessageBuilder.withPayload(cloudPlatform)
        .setHeader("path", vfs.getCloudPath())
        .setHeader("fileName", vfs.getFileName())
        .setHeader("bucketName", vfs.getBucketName())
        .setHeader("vfsId", vfs.getId())
        .build();
  }

  @Override
  public Object downloadFromAws(Message<String> message) {
    MessageHeaders headers = message.getHeaders();
    String path = headers.get("path", String.class);
    String bucket = headers.get("bucketName", String.class);
    ClientConfiguration configuration = headers.get("credentials", ClientConfiguration.class);
    assert configuration != null;
    Map<String, String> decryptCredentials = this.decryptCredentials(configuration);
    var s3Client = getS3Client(decryptCredentials);
    log.info("Credentials: {}", decryptCredentials);
    try {
      ResponseInputStream<GetObjectResponse> respIO =
          s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(path).build());
      ByteArrayOutputStream fileOut = new ByteArrayOutputStream();
      long l = respIO.transferTo(fileOut);
      GetObjectResponse response = respIO.response();
      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(response.contentType()))
          .contentLength(l)
          .header(HttpHeaders.CONTENT_DISPOSITION, response.contentDisposition())
          .body(fileOut.toByteArray());

    } catch (Exception ex) {
      return ResponseEntity.internalServerError()
          .body(
              ResponseWrapper.builder()
                  .message("Error Downloading file.")
                  .success(true)
                  .payload(Map.of("message", ex.getMessage()))
                  .build());
    }
  }

  @Override
  public Object downloadFromGcp(Message<?> message) throws IOException {
    MessageHeaders headers = message.getHeaders();
    ClientConfiguration configuration =
        message.getHeaders().get("credentials", ClientConfiguration.class);
    assert configuration != null;
    Map<String, String> decryptCredentials = this.decryptCredentials(configuration);
    log.info("Credentials: {}", decryptCredentials);
    String bucket = headers.get("bucketName", String.class);
    String fileName = headers.get("fileName", String.class);
    String clientId = decryptCredentials.get("client_id");
    String clientEmail = decryptCredentials.get("client_email");
    String privateKeyId = decryptCredentials.get("private_key_id");
    String privateKey = decryptCredentials.get("private_key");
    ServiceAccountCredentials serviceAccountCredentials =
        ServiceAccountCredentials.fromPkcs8(
            clientId,
            clientEmail,
            privateKey,
            privateKeyId,
            Collections.singletonList(StorageScopes.DEVSTORAGE_FULL_CONTROL));
    Storage storage =
        StorageOptions.newBuilder().setCredentials(serviceAccountCredentials).build().getService();
    if (bucket != null && fileName != null) {
      Blob blob = storage.get(BlobId.of(bucket, fileName));
      String contentDisposition = blob.getContentDisposition();
      String contentType = blob.getContentType();
      if (contentDisposition == null) {
        contentDisposition = "attachment; filename=" + fileName;
      }
      log.info("Content Type: {}, Content Disposition: {}", contentType, contentDisposition);
      byte[] content = blob.getContent(Blob.BlobSourceOption.shouldReturnRawInputStream(true));
      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(contentType))
          .contentLength(blob.getSize())
          .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
          .body(content);
    } else {
      return ResponseEntity.badRequest()
          .body(
              ResponseWrapper.builder()
                  .payload(Map.of("message", "Either bucket or filename is null"))
                  .success(false)
                  .message("Error Downloading file")
                  .build());
    }
  }

  @Override
  public Object downloadFromAzure(Message<?> message) {
    MessageHeaders headers = message.getHeaders();
    String fileName = headers.get("fileName", String.class);
    String path = headers.get("path", String.class);
    ClientConfiguration configuration =
        message.getHeaders().get("credentials", ClientConfiguration.class);

    assert configuration != null;
    Map<String, String> decryptCredentials = this.decryptCredentials(configuration);
    log.info("Credentials: {}", decryptCredentials);
    String accountName = decryptCredentials.get("storage_account");
    String connection_string = decryptCredentials.get("connection_string");
    assert path != null;
    String url =
        UriComponentsBuilder.newInstance()
            .scheme("https")
            .host(accountName + ".blob.core.windows.net")
            .path(path)
            .build()
            .toUriString();
    log.info("Url: {}", url);

    BlobClient blobClient =
        new BlobClientBuilder().endpoint(url).connectionString(connection_string).buildClient();
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      blobClient.downloadStream(outputStream);
      BlobProperties properties = blobClient.getProperties();
      String blobContentType = properties.getContentType();
      String contentDisposition = properties.getContentDisposition();
      if (contentDisposition == null) {
        contentDisposition = "attachment; filename=" + fileName;
      }
      log.info("Content Type: {}, Content Disposition: {}", blobContentType, contentDisposition);
      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(blobContentType))
          .contentLength(properties.getBlobSize())
          .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
          .body(outputStream.toByteArray());
    } catch (IOException e) {
      return ResponseEntity.internalServerError()
          .body(
              ResponseWrapper.builder()
                  .payload(Map.of("message", e.getMessage()))
                  .success(false)
                  .message("Error Downloading file")
                  .build());
    }
  }

  private Map<String, String> decryptCredentials(ClientConfiguration clientConfiguration) {
    Map<String, String> credentials = new HashMap<>();
    String encryptedKeys = clientConfiguration.getEncryptedFields();
    clientConfiguration
        .getCredentials()
        .forEach(
            (key, value) -> {
              if (encryptedKeys == null) {
                credentials.put(key, value);
              } else if (encryptedKeys.equals("all")) {
                credentials.put(key, encryptionUtils.decrypt(value));
              } else {
                List<String> encryptedFieldList =
                    Arrays.stream(encryptedKeys.split(",")).collect(Collectors.toList());
                if (encryptedFieldList.contains(key)) {
                  credentials.put(key, encryptionUtils.decrypt(value));
                } else {
                  credentials.put(key, value);
                }
              }
            });
    return credentials;
  }
}
