package com.themuler.fs.internal.repository;

import com.themuler.fs.internal.model.TempDownload;
import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempDownloadRepository extends CrudRepository<TempDownload, ObjectId> {}
