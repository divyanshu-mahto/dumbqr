package com.dumbqr.dumbqr.service;

import io.github.cdimascio.dotenv.Dotenv;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GeolocationService {

    Dotenv dotenv = Dotenv.load();

    public ResponseEntity<String> getGeolocation(String ip){
        try{
            HttpResponse<JsonNode> response = Unirest.get("https://api.ipgeolocation.io/ipgeo?apiKey={API_KEY}&ip={ip}&fields=country_name")
                    .routeParam("API_KEY", dotenv.get("API_KEY"))
                    .routeParam("ip", ip)
                    .asJson();

            if(response.isSuccess()){
                JSONObject jsonObject = response.getBody().getObject();
                return new ResponseEntity<>(jsonObject.optString("country_name","Location unavailable"), HttpStatus.OK);
            }else{
                return new ResponseEntity<>("Location unavailable", HttpStatus.OK);
            }
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
