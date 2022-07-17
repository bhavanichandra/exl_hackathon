package com.themuler.fs.internal.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.themuler.fs.api.UserRole;
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
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "user")
public class AppUser {
  @Id private ObjectId id;

  @Indexed(unique = true)
  private String email;

  private String password;
  private String name;

  @Field("client_id")
  @DocumentReference(collection = "client")
  private Client client;

  @BsonProperty(value = "user_role")
  private UserRole role;

  public String getId() {
    return id.toString();
  }
}
