package com.themuler.fs.internal.model;

import com.themuler.fs.api.CloudPlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "client")
public class Client {
  @MongoId(targetType = FieldType.STRING)
  @Id
  private ObjectId id;

  public String getId() {
    return id.toString();
  }

  @Indexed private String name;

  @BsonProperty(value = "cloud_platform")
  private CloudPlatform cloudPlatform;

  @DocumentReference(collection = "client_config")
  private List<ClientConfiguration> clientConfigurations;
}
