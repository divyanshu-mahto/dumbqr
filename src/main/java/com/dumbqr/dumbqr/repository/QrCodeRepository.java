package com.dumbqr.dumbqr.repository;

import com.dumbqr.dumbqr.model.QrCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QrCodeRepository extends MongoRepository<QrCode, ObjectId> {
    QrCode findByShortUrl(String shortUrl);
}
