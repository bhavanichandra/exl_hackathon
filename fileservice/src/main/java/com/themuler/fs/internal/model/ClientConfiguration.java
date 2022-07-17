package com.themuler.fs.internal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "client_config")
public class ClientConfiguration {
  @Id
  private ObjectId id;

  private String environment;

  private Map<String, String> credentials;
}
