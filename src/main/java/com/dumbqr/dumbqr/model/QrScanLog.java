package com.dumbqr.dumbqr.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "qr_scan_logs")
@Data
public class QrScanLog {
    @Id
    private ObjectId id;
    private String shortId;
    private LocalDateTime timestamp;
    private String ip;
    private String location;
    private String userAgent;
}
