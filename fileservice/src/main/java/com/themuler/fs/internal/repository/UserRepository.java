package com.themuler.fs.internal.repository;

import com.themuler.fs.internal.model.AppUser;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<AppUser, ObjectId> {
  AppUser findByEmail(String email);

}
