package com.softwaredevtools.standbot.service;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class StandbotCustomAuthenticationService {

    private JWTService _jwtService;

    @Inject
    public StandbotCustomAuthenticationService(JWTService jwtService) {
        _jwtService = jwtService;
    }

    public boolean isValid(String jwt) {
        try {
            _jwtService.parseJWT(jwt);
            return true;
        } catch (Exception e) {
            System.out.println("Couldn't decode jwt:" + jwt);
            return false;
        }
    }

}
