package com.themuler.fs.internal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "client_config")
public class ClientConfiguration {
  @MongoId(targetType = FieldType.STRING)
  @Id
  private ObjectId id;

  public String getId() {
    return id.toString();
  }

  private String environment;
  private String encryptedFields;
  private Map<String, String> credentials;
}
