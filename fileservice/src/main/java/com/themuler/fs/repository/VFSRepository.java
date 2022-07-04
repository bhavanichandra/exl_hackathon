package com.themuler.fs.repository;

import com.themuler.fs.model.VirtualFileSystem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VFSRepository extends CrudRepository<VirtualFileSystem, Long> {}
