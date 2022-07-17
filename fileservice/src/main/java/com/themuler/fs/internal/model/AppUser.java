package com.themuler.fs.internal.model;

import com.themuler.fs.api.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "user")
public class AppUser {
  @MongoId(targetType = FieldType.STRING)
  @Id
  private ObjectId id;

  public String getId() {
    return id.toString();
  }

  @Indexed(unique = true)
  private String email;

  @BsonIgnore
  private String password;

  private String name;

  @Field("client_id")
  @DocumentReference(collection = "client")
  private Client client;

  @BsonProperty(value = "user_role")
  private UserRole role;

}
