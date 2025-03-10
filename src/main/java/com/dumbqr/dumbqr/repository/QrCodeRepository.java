package com.dumbqr.dumbqr.repository;

import com.dumbqr.dumbqr.model.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    QrCode findByShortId(String shortId);
}