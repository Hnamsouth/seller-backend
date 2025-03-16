package com.vtp.vipo.seller.config.security.jwt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vtp.vipo.seller.common.utils.FileUtils;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import java.time.Instant;
import java.util.HashMap;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.ObjectUtils;

@Service("jwtTokenService")
@Slf4j
@Getter
public class JwtTokenService {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);

    @Value("${security.vipo.secret-key.class-path}")
    private String vipoSecretKeyClassPath;

    @Value("${security.vipo.exp.jwt-access}")
    private Long jwtExpirationMs;
    public static final String USER_INFO_FIELD_TOKEN = "userInfo";

    @SneakyThrows
    public String generateTokenFromUserInfo(String id) {
        String vipoSecretKey = getKeyString(vipoSecretKeyClassPath, true);
        return Jwts.builder().setSubject(id).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)).signWith(SignatureAlgorithm.HS256, vipoSecretKey)
                .compact();
    }

    @SneakyThrows
    public String generateTokenFromUserInfo(VipoUserDetails userDetails) {
        String vipoSecretKey = getKeyString(vipoSecretKeyClassPath, true);
        Map<String, Object> body = new HashMap<>();
        body.put(USER_INFO_FIELD_TOKEN, JsonMapperUtils.writeValueAsString(userDetails));
        return Jwts.builder().setClaims(body).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)).signWith(SignatureAlgorithm.HS256, vipoSecretKey)
                .compact();
    }


    @SneakyThrows
    public VipoUserDetails getUserInfoFromToken(String authToken) {
        VipoUserDetails details = null;
        String vipoSecretKey = getKeyString(vipoSecretKeyClassPath, true);
        Map<String, Object> body = Jwts.parser().setSigningKey(vipoSecretKey).parseClaimsJws(authToken).getBody();
        if(!CollectionUtils.isEmpty(body)){
            details = JsonMapperUtils.convertJsonToObject(body.get(USER_INFO_FIELD_TOKEN).toString(), VipoUserDetails.class);
        }
        return details;
    }

    private String getKeyString(String pathOrKey, Boolean isFile) throws Exception {
        try {
            String privateKey = Boolean.TRUE.equals(isFile) ? FileUtils.fullyReadFileFromClassPath(pathOrKey) : pathOrKey.strip();
            // Define a single regular expression for all patterns to replace
            privateKey = privateKey.replaceAll("-----BEGIN (RSA )?SECRET KEY-----|-----END (RSA )?SECRET KEY-----|-----BEGIN (RSA )?PRIVATE KEY-----|-----END (RSA )?PRIVATE KEY-----|\n|\r", "");
            return privateKey;
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage(), ex);
            throw new Exception("Error occurred while reading the key: " + ex.getMessage());
        }
    }

}
