package com.dumbqr.dumbqr.model;


import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
public class User {

    @Id
    private ObjectId id;
    private String email;
    private String password;

    private List<ObjectId> qrcodes = new ArrayList<>();
}
