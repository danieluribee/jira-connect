package com.softwaredevtools.standbot.service;

import com.softwaredevtools.standbot.config.StandbotConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;

@Service
public class JWTService {
    public JWTService() {

    }

    public String sign(HashMap<String, Object> data) {
        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
        //Let's set the JWT Claims
        JwtBuilder builder = null;
        builder = Jwts.builder()
                .setClaims(data)
                .setIssuedAt(now)
                .signWith(signatureAlgorithm, StandbotConfig.JWT_SECRET.getBytes());

        //if it has been specified, let's add the expiration
        long expMillis = nowMillis + 30000;
        Date exp = new Date(expMillis);
        builder.setExpiration(exp);

        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    //Sample method to validate and read the JWT
    public Claims parseJWT(String jwt) {

        //This line will throw an exception if it is not a signed JWS (as expected)
        return Jwts.parser()
                .setSigningKey(StandbotConfig.JWT_SECRET.getBytes())
                .parseClaimsJws(jwt).getBody();
    }
}
