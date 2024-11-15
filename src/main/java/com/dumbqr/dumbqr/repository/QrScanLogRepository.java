package com.dumbqr.dumbqr.repository;

import com.dumbqr.dumbqr.model.QrScanLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QrScanLogRepository extends MongoRepository<QrScanLog, ObjectId> {

}
