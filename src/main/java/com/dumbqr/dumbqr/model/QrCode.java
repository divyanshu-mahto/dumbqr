package com.dumbqr.dumbqr.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "qr_codes")
@Data
public class QrCode implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "short_id", nullable = false, unique = true)
    private String shortId;

    @Column(name = "redirect_url", nullable = false)
    private String redirectUrl;

    @Column(name = "qrcode")
    private byte[] qrcode;

    @Column(name = "foreground")
    private String foreground;

    @Column(name = "background")
    private String background;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "qrCode", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<QrScanLog> scansLogs = new ArrayList<>();
}

////0xFF000002, 0xFFFFFFFF