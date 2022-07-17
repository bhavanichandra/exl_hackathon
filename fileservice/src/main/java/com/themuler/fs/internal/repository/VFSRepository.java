package com.themuler.fs.internal.repository;

import com.themuler.fs.internal.model.AppUser;
import com.themuler.fs.internal.model.Client;
import com.themuler.fs.internal.model.VirtualFileSystem;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VFSRepository extends MongoRepository<VirtualFileSystem, ObjectId> {
  VirtualFileSystem findByFileNameLikeIgnoreCaseAndClient(String fileName, Client client);

  List<VirtualFileSystem> findAllByClientAndUser(Client client, AppUser appUser);
}
