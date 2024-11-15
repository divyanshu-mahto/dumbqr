package com.dumbqr.dumbqr.service;

import com.dumbqr.dumbqr.model.QrCode;
import com.dumbqr.dumbqr.model.QrScanLog;
import com.dumbqr.dumbqr.model.User;
import com.dumbqr.dumbqr.repository.QrCodeRepository;
import com.dumbqr.dumbqr.repository.QrScanLogRepository;
import com.dumbqr.dumbqr.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class QrCodeService {

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QrScanLogRepository qrScanLogRepository;

    public QrCode getQrCodeObject(String shortId){
        return qrCodeRepository.findByShortUrl(shortId);
    }

    public byte[] getQrCodeImage(String text, int width, int height, String foreground, String  background) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageConfig config = new MatrixToImageConfig(
                (int) Long.parseLong(foreground.replace("0x", ""), 16),
                (int) Long.parseLong(background.replace("0x", ""), 16)
//                0xFF000002,0xFFFFFFFF
        );
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream, config);
        return pngOutputStream.toByteArray();
    }

    public ResponseEntity<byte[]> getQr(User user, String shortid){
        try{
            List<ObjectId> qrids = user.getQrcodes();
            QrCode qrCode = qrCodeRepository.findByShortUrl(shortid);
            if(qrCode == null){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            if(qrids.contains(qrCode.getId())){
                byte[] qrImage = qrCode.getQrcode().getData();
                return ResponseEntity
                        .ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(qrImage);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String addQr(QrCode qrCode) throws IOException, WriterException {

        byte[] qrImage = getQrCodeImage("http://localhost:8080/goto/"+qrCode.getShortUrl(), 250, 250, qrCode.getForeground(), qrCode.getBackground());
        qrCode.setQrcode(new Binary(BsonBinarySubType.BINARY, qrImage));

        qrCode = qrCodeRepository.insert(qrCode);
        return "QR Code created with ID: " + qrCode.getId();
    }

    public QrCode getQr(ObjectId id){
        return qrCodeRepository.findById(id).get();
    }

    public void createQR(QrCode qrCode){
        qrCodeRepository.save(qrCode);
    }

    //check if some short id is avaliable to take or not
    public ResponseEntity<String> checkShortId(String shortUrl){
        QrCode qrCode = qrCodeRepository.findByShortUrl(shortUrl);
        if (qrCode != null){
            return new ResponseEntity<>("Short url already taken",HttpStatus.NOT_ACCEPTABLE);
        }else{
            return new ResponseEntity<>("Short url available", HttpStatus.OK);
        }
    }

    //verify if a shortid is valid or not for redirecting
    public ResponseEntity<String> verifyShortId(String shortUrl){
        QrCode qrCode = qrCodeRepository.findByShortUrl(shortUrl);
        if (qrCode != null){
            return new ResponseEntity<>("ShortId exists", HttpStatus.OK);
        }else{
            return new ResponseEntity<>("ShortId does not exist", HttpStatus.NOT_FOUND);
        }
    }

    public String getRedirectUrl(String shortId){
        QrCode qrCode = qrCodeRepository.findByShortUrl(shortId);
        if (qrCode == null) {
            throw new RuntimeException("QR Code not found for shortid: " + shortId);
        }

        return qrCode.getRedirectUrl();
    }

    public ResponseEntity<String> deleteQrCode(User user, String shortId){
        QrCode qrCode = qrCodeRepository.findByShortUrl(shortId);
        if (qrCode == null){
            return new ResponseEntity<>("Qr code not found", HttpStatus.NOT_FOUND);
        }

        //check if the QR code belongs to the user
        if(user.getQrcodes().contains(qrCode.getId())){

            List<ObjectId> qrids = user.getQrcodes();
            qrids.remove(qrCode.getId());
            user.setQrcodes(qrids);
            userRepository.save(user);

            //delete the scans
            List<ObjectId> scans = qrCode.getScans();
            for (ObjectId scan : scans){
                qrScanLogRepository.deleteById(scan);
            }

            //delete the qr code
            qrCodeRepository.deleteById(qrCode.getId());


            return new ResponseEntity<>("Successfully Deleted",HttpStatus.OK);

        }else {
            return new ResponseEntity<>("QR code not found",HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> updateQrCode(User user, String shortId, String newRedirectUrl) {
        QrCode qrCode = qrCodeRepository.findByShortUrl(shortId);
        if (qrCode == null){
            return new ResponseEntity<>("Qr code not found", HttpStatus.NOT_FOUND);
        }

        //check if the QR code belongs to the user
        if(user.getQrcodes().contains(qrCode.getId())){
            qrCode.setRedirectUrl(newRedirectUrl);
            qrCodeRepository.save(qrCode);
            return new ResponseEntity<>("Redirect url updated",HttpStatus.OK);

        }else {
            return new ResponseEntity<>("QR code not found",HttpStatus.NOT_FOUND);
        }

    }

    //update qr code scans
    public ResponseEntity<?> addQrScan(QrCode qrCode, QrScanLog qrScanLog){
        List<ObjectId> scans = qrCode.getScans();
        scans.add(qrScanLog.getId());
        qrCode.setScans(scans);
        qrCodeRepository.save(qrCode);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //get analytics
    public ResponseEntity<?> getAnalytics(QrCode qrCode){

        List<ObjectId> scans = qrCode.getScans();
        List<QrScanLog> analytics = new ArrayList<>();

        for (ObjectId id : scans) {
            qrScanLogRepository.findById(id).ifPresent(analytics::add);
        }

        if (analytics.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(analytics, HttpStatus.OK);
    }
}

