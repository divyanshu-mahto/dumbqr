package com.dumbqr.dumbqr.model;

import lombok.Data;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "qrCodes")
@Data
public class QrCode {
    @Id
    private ObjectId id;
    private String shortUrl;
    private String redirectUrl;
    private Binary qrcode;
    private String foreground;
    private String background;
    private List<ObjectId> scans = new ArrayList<>();
}

////0xFF000002, 0xFFFFFFFF