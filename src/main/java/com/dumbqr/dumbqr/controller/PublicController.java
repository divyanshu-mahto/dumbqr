package com.dumbqr.dumbqr.controller;

import com.dumbqr.dumbqr.model.QrCode;
import com.dumbqr.dumbqr.model.QrScanLog;
import com.dumbqr.dumbqr.repository.QrScanLogRepository;
import com.dumbqr.dumbqr.service.BloomFilterService;
import com.dumbqr.dumbqr.service.GeolocationService;
import com.dumbqr.dumbqr.service.QrCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@RestController
public class PublicController {

    @Autowired
    QrCodeService qrCodeService;

    @Autowired
    GeolocationService geolocationService;

    @Autowired
    QrScanLogRepository qrScanLogRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private BloomFilterService bloomFilterService;

    //Homepage
    @GetMapping("/")
    public ResponseEntity<?> homepage(HttpServletResponse response) throws IOException {
        try{
            response.sendRedirect(frontendUrl);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Redirect link noAuth
    @GetMapping("goto/{shortId}")
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity<?> redirectOld(@PathVariable String shortId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            String cachedRedirectUrl = redisTemplate.opsForValue().get(shortId);

            if(cachedRedirectUrl != null){

                //perform log write
                logScan(request, shortId, LocalDateTime.now());

                response.sendRedirect(cachedRedirectUrl);
                return new ResponseEntity<>(HttpStatus.OK);
            }

            QrCode qrCode = qrCodeService.getQrCodeObject(shortId);
            if(qrCode == null){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            logScan(request, shortId, LocalDateTime.now());
            redisTemplate.opsForValue().set(shortId, qrCode.getRedirectUrl(), Duration.ofMinutes(10));
            response.sendRedirect(qrCode.getRedirectUrl());
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{shortId}")
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity<?> redirect(@PathVariable String shortId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            String cachedRedirectUrl = redisTemplate.opsForValue().get(shortId);

            if(cachedRedirectUrl != null){

                //perform log write
                logScan(request, shortId, LocalDateTime.now());

                response.sendRedirect(cachedRedirectUrl);
                return new ResponseEntity<>(HttpStatus.OK);
            }

            //bloom filter
            if(!bloomFilterService.lookUp(shortId)){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            QrCode qrCode = qrCodeService.getQrCodeObject(shortId);
            if(qrCode == null){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            logScan(request, shortId, LocalDateTime.now());
            redisTemplate.opsForValue().set(shortId, qrCode.getRedirectUrl(), Duration.ofMinutes(10));
            response.sendRedirect(qrCode.getRedirectUrl());
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Async
    public void logScan(HttpServletRequest request, String shortId, LocalDateTime time){

        try{
            QrCode qrCode = qrCodeService.getQrCodeObject(shortId);
            if(qrCode == null) return;

            GeolocationService.LocationInfo location = geolocationService.getGeolocation(request.getRemoteAddr()).getBody();

            QrScanLog qrScanLog = new QrScanLog();
            qrScanLog.setTimestamp(time);
            qrScanLog.setIp(request.getRemoteAddr());
            qrScanLog.setUserAgent(request.getHeader("User-Agent"));
            qrScanLog.setCountry(location.country);
            qrScanLog.setState(location.state);
            qrScanLog.setCity(location.city);
            qrScanLog.setQrCode(qrCode);

            qrCode.getScansLogs().add(qrScanLog);
            qrScanLogRepository.save(qrScanLog);

        } catch (Exception e){
            System.out.println("Logging failed: "+e.getMessage());
        }
    }

    @RequestMapping("/bloom/add/{st}")
    public ResponseEntity<?> bloomAdd(@PathVariable String st){
        try{
            bloomFilterService.add(st);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("/bloom/look/{st}")
    public ResponseEntity<?> bloomCheck(@PathVariable String st){
        try{
            return new ResponseEntity<>(bloomFilterService.lookUp(st), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
