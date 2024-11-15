package com.dumbqr.dumbqr.repository;

import com.dumbqr.dumbqr.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {

    User findByEmail(String email);
}
