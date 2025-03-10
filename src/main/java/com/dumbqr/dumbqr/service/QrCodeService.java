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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Service
public class QrCodeService {

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QrScanLogRepository qrScanLogRepository;

    @Value("${shorturl.prefix}")
    private String shortUrlPrefix;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private BloomFilterService bloomFilterService;

    public byte[] getQrCodeImage(String text, int width, int height, String foreground, String  background) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageConfig config = new MatrixToImageConfig(
                (int) Long.parseLong(foreground.replace("0x", ""), 16),
                (int) Long.parseLong(background.replace("0x", ""), 16)
        );
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream, config);
        return pngOutputStream.toByteArray();
    }

    //api to display QR code
    public ResponseEntity<byte[]> getQr(User user, String shortId){
        try{
            QrCode qrCode = qrCodeRepository.findByShortId(shortId);
            if(qrCode == null){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            if(user.getQrCodes().contains(qrCode)){
                byte[] qrImage = qrCode.getQrcode();
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


    @Transactional
    public String createQr(QrCode qrCode) throws IOException, WriterException {

        byte[] qrImage = getQrCodeImage(shortUrlPrefix+qrCode.getShortId(), 250, 250, qrCode.getForeground(), qrCode.getBackground());

        if (qrImage == null || qrImage.length == 0) {
            throw new IllegalStateException("Failed to generate QR code image");
        }

        qrCode.setQrcode(qrImage);

        qrCode = qrCodeRepository.save(qrCode);

        redisTemplate.opsForValue().set(qrCode.getShortId(), qrCode.getRedirectUrl(), Duration.ofMinutes(10));
        bloomFilterService.add(qrCode.getShortId());
        return qrCode.getRedirectUrl();
    }

    public byte[] getQrimage(String shortId, String foreground, String background) throws IOException, WriterException {

        byte[] qrImage = getQrCodeImage(shortUrlPrefix+shortId, 250, 250, foreground, background);

        if (qrImage == null || qrImage.length == 0) {
            throw new IllegalStateException("Failed to generate QR code image");
        }
        return qrImage;
    }

    //return qr code object by shortId
    public QrCode getQrCodeObject(String shortId){
        return qrCodeRepository.findByShortId(shortId);
    }

    public ResponseEntity<String> deleteQrCode(User user, String shortId){
        QrCode qrCode = qrCodeRepository.findByShortId(shortId);
        if (qrCode == null){
            return new ResponseEntity<>("Qr code not found", HttpStatus.NOT_FOUND);
        }

        //check if the QR code belongs to the user
        if(qrCode.getUser().equals(user)){

            user.getQrCodes().remove(qrCode);
            qrCodeRepository.deleteById(qrCode.getId());

            redisTemplate.delete(shortId);

            return new ResponseEntity<>("Successfully Deleted",HttpStatus.OK);

        }else {
            return new ResponseEntity<>("QR code not found",HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> updateQrCode(User user, String shortId, String newRedirectUrl, String newName) {
        QrCode qrCode = qrCodeRepository.findByShortId(shortId);
        if (qrCode == null){
            return new ResponseEntity<>("Qr code not found", HttpStatus.NOT_FOUND);
        }

        //check if the QR code belongs to the user
        if(user.getQrCodes().contains(qrCode)){
            qrCode.setRedirectUrl(newRedirectUrl);
            qrCode.setName(newName);
            qrCodeRepository.save(qrCode);

            redisTemplate.opsForValue().set(shortId, newRedirectUrl, Duration.ofMinutes(10));

            return new ResponseEntity<>("Redirect url updated",HttpStatus.OK);

        }else {
            return new ResponseEntity<>("QR code not found",HttpStatus.NOT_FOUND);
        }

    }

    //get analytics api
    public ResponseEntity<?> getAnalytics(QrCode qrCode){

        List<QrScanLog> analytics = qrCode.getScansLogs();

        return new ResponseEntity<>(analytics, HttpStatus.OK);
    }
}

