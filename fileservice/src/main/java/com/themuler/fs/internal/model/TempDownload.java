package com.themuler.fs.internal.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Document(value = "temp_download")
public class TempDownload {

  @Id private ObjectId id;

  @Indexed private String fileName;
  private String bucketName;
  private String cloudPlatform;
  private Instant startTime;
  private Instant endTime;
  private Boolean isAvailable;

  private String downloadUrl;

  @Field("user_id")
  @DocumentReference(collection = "user")
  private AppUser user;

  private String status;

  private String message;

  private TempDownload(String fileName, String bucketName, String cloudPlatform) {
    this.bucketName = bucketName;
    this.startTime = Instant.now();
    this.cloudPlatform = cloudPlatform;
    this.fileName = fileName;
  }

  public static TempDownload initialize(String fileName, String bucketName, String cloudPlatform) {
    return new TempDownload(fileName, bucketName, cloudPlatform);
  }

  public void onError(String message) {
    this.message = message;
    this.status = "ERROR";
  }

  public void makeDownloadAvailable(String url) {
    this.isAvailable = true;
    this.downloadUrl = url;
    this.status = "DOWNLOAD_LINK_SHARED";
  }

  public void removeAccess() {
    this.isAvailable = false;
    this.downloadUrl = null;
    this.status = "REMOVED_ACCESS";
    this.endTime = Instant.now();
  }
}
