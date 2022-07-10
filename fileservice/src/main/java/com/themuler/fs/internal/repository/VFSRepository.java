package com.themuler.fs.internal.repository;

import com.themuler.fs.internal.model.VirtualFileSystem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VFSRepository extends CrudRepository<VirtualFileSystem, Long> {}
