package com.sprect.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface JwtService {
    @SneakyThrows
    Map<String, Object> getNewTokens(String accessToken, String refreshToken);

    Map<String, Object> createTokens(String username, List<String> types);

    Authentication getAuthentication(String token);

    Jws<Claims> getClaims(String token);

    String resolveToken(HttpServletRequest req);

    void validateAccessToken(String token) throws ExpiredJwtException;

    void addBlackList(String token);
}
