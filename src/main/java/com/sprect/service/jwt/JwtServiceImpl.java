package com.sprect.service.jwt;

import com.sprect.model.entity.Role;
import com.sprect.model.entity.User;
import com.sprect.model.redis.AccessKey;
import com.sprect.model.redis.BlackListTokens;
import com.sprect.model.redis.RefreshKey;
import com.sprect.repository.nosql.AccessKeyRepository;
import com.sprect.repository.nosql.BlackListRepositories;
import com.sprect.repository.nosql.RefreshKeyRepository;
import com.sprect.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.sprect.utils.DefaultString.*;

@Component
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.accessToken.expired}")
    private long validityInMillisecondsAccess;
    @Value("${jwt.refreshToken.expired}")
    private long validityInMillisecondsRefresh;

    private final UserService userService;
    private final RefreshKeyRepository refreshKeyRepository;
    private final AccessKeyRepository accessKeyRepository;
    private final BlackListRepositories blackListRepositories;

    public JwtServiceImpl(UserService userService,
                          RefreshKeyRepository refreshKeyRepository,
                          AccessKeyRepository accessKeyRepository,
                          BlackListRepositories blackListRepositories) {
        this.userService = userService;
        this.refreshKeyRepository = refreshKeyRepository;
        this.accessKeyRepository = accessKeyRepository;
        this.blackListRepositories = blackListRepositories;
    }

    @Override
    @SneakyThrows
    public Map<String, Object> getNewTokens(String accessToken, String refreshToken) {
        try {
            validateAccessToken(accessToken);
        } catch (JwtException e) {
            if (e.getMessage().equals(ACCESS_EXPIRED)) {
                validateRefreshToken(refreshToken);

                String body = refreshToken.split("\\.")[1];
                byte[] encodeBody = Base64.getDecoder().decode(body.getBytes(StandardCharsets.UTF_8));
                HashMap<String, Object> claims = new ObjectMapper().readValue(new String(encodeBody), HashMap.class);

                String nameAccess = claims.get("sub").toString();
                String nameRefresh = getClaims(refreshToken).getBody().getSubject();

                if (nameAccess.equals(nameRefresh)) {
                    return createTokens(getClaims(refreshToken).getBody().getSubject(), List.of("access", "refresh"));
                }
            }
            throw new JwtException(e.getMessage());
        }
        throw new JwtException(ACCESS_NOT_EXPIRED);
    }

    @Override
    public Map<String, Object> createTokens(String username, List<String> types) {
        User user = userService.findUserByUEP(username);
        List<Role> roles = new ArrayList<>(user.getRole());

        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put("id", user.getIdUser());
        claims.put("roles", getRoleNames(roles));

        Map<String, Object> response = new HashMap<>();

        for (String type : types) {
            response.put(type + "Token", createToken(
                    user.getUsername(),
                    claims,
                    getRandomKey(user.getUsername(), type),
                    getExpired(type),
                    type));
        }

        if (response.size() == 2) {
            response.put("user", user);
        }
        return response;
    }

    @Override
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userService.loadUserByUsername(getClaims(token).getBody().getSubject(), "JWT");
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    @Override
    public Jws<Claims> getClaims(String token) {
        if (blackListRepositories.findById(token).isPresent()) {
            throw new JwtException(TOKEN_BLACK_LIST);
        }

        return Jwts.parserBuilder()
                .setSigningKeyResolver(new KeyResolver())
                .build()
                .parseClaimsJws(token);
    }

    @Override
    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer_")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    public void validateAccessToken(String token) throws ExpiredJwtException {
        if (blackListRepositories.findById(token).isPresent()) {
            throw new JwtException(TOKEN_BLACK_LIST);
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKeyResolver(new KeyResolver())
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new JwtException(ACCESS_EXPIRED);
        } catch (JwtException e) {
            throw new JwtException(ACCESS_INVALID);
        }
    }

    private void validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKeyResolver(new KeyResolver())
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new JwtException(REFRESH_EXPIRED);
        } catch (JwtException e) {
            throw new JwtException(REFRESH_INVALID);
        }
    }

    @Override
    public void addBlackList(String token) {
        blackListRepositories.save(new BlackListTokens(token, ""));
    }

    private String createToken(String username,
                               Claims claims,
                               SecretKey key,
                               Date expired,
                               String type) {

        return Jwts.builder()
                .setHeaderParam(JwsHeader.KEY_ID, username)
                .setHeaderParam(JwsHeader.TYPE, type)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expired)
                .signWith(key)
                .compact();
    }

    private Date getExpired(String type) {
        Date now = new Date();
        if (type.equals("access")) {
            return new Date(now.getTime() + validityInMillisecondsAccess);
        }
        return new Date(now.getTime() + validityInMillisecondsRefresh);
    }

    private List<String> getRoleNames(List<Role> userRoles) {
        return userRoles.stream()
                .map(Role::getNameRole)
                .collect(Collectors.toList());
    }

    private SecretKey getRandomKey(String username, String refresh) {
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        if (refresh.equals("refresh")) {
            refreshKeyRepository.save(new RefreshKey(username, encodedKey));
        } else {
            accessKeyRepository.save(new AccessKey(username, encodedKey));
        }

        return secretKey;
    }

    private SecretKey getAccessKey(String id) {
        Optional<AccessKey> byId = accessKeyRepository.findById(id);
        String encodedKey = byId.map(AccessKey::getAccessKey).orElse(null);
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    private SecretKey getRefreshKey(String id) {
        Optional<RefreshKey> byId = refreshKeyRepository.findById(id);
        String encodedKey = byId.map(RefreshKey::getRefreshKey).orElse(null);
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    private class KeyResolver extends SigningKeyResolverAdapter {
        public SecretKey resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
            String keyId = jwsHeader.getKeyId();

            if (jwsHeader.getType().equals("refresh")) {
                return getRefreshKey(keyId);
            }
            return getAccessKey(keyId);
        }
    }
}
