package com.themuler.fs.internal.repository;

import com.themuler.fs.internal.model.VirtualFileSystem;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VFSRepository extends MongoRepository<VirtualFileSystem, ObjectId> {
  VirtualFileSystem findByFileNameLikeIgnoreCase(String fileName);
}
