package com.dumbqr.dumbqr.service;

import io.github.cdimascio.dotenv.Dotenv;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class GeolocationService {

    public class LocationInfo{
        public String country;
        public String state;
        public String city;

        public LocationInfo() {
            this.country = "Unknown";
            this.state = "Unknown";
            this.city = "Unknown";
        }
    }

    @Value("${ipgeolocation.apiKey}")
    private String apiKey;

    public ResponseEntity<LocationInfo> getGeolocation(String ip){
        LocationInfo locationInfo = new LocationInfo();
        try{
            HttpResponse<JsonNode> response = Unirest.get("https://api.ipgeolocation.io/ipgeo?apiKey={API_KEY}&ip={ip}&fields=country_name,state_prov,city")
                    .routeParam("API_KEY", apiKey)
                    .routeParam("ip", ip)
                    .asJson();

            if(response.isSuccess()){
                JSONObject jsonObject = response.getBody().getObject();
                locationInfo.country = jsonObject.optString("country_name","Unknown");
                locationInfo.state = jsonObject.optString("state_prov","Unknown");
                locationInfo.city = jsonObject.optString("city","Unknown");
            }
            return new ResponseEntity<>(locationInfo, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
