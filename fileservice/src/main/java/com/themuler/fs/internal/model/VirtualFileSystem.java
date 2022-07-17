package com.themuler.fs.internal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "vfs")
public class VirtualFileSystem {

  @Id private ObjectId id;

  @Indexed
  @BsonProperty(value = "file_name")
  private String fileName;

  @BsonProperty(value = "cloud_path")
  private String cloudPath;

  @BsonProperty(value = "stored_bucket_name")
  private String bucketName;

  private String status;

  private String cloudPlatform;

  @BsonProperty(value = "local_path")
  private String localPath;

  @BsonProperty(value = "vfs_type")
  private String vfsType;

  @Field("client_id")
  @DocumentReference(collection = "client")
  private Client client;
}
