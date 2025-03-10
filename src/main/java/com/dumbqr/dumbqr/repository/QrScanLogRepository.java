package com.dumbqr.dumbqr.repository;

import com.dumbqr.dumbqr.model.QrScanLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QrScanLogRepository extends JpaRepository<QrScanLog, Long> {

}
