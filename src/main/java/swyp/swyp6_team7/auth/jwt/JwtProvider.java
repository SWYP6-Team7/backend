package swyp.swyp6_team7.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;

import java.util.Base64;
import java.util.List;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {
    private  final byte[] secretKey;
    private final long accessTokenValidity = 15 * 60 * 1000; // 15분
    private final long refreshTokenValidity = 7 * 24 * 60 * 60 * 1000; // 1주일
    private final JwtBlacklistService jwtBlacklistService;

    public JwtProvider(@Value("${custom.jwt.secretKey}") String secretKey,JwtBlacklistService jwtBlacklistService){
        this.secretKey = Base64.getDecoder().decode(secretKey);
        this.jwtBlacklistService = jwtBlacklistService;
    }

    // Access Token 생성
    public String createAccessToken(String userEmail, Integer userNumber, List<String> roles) {
        return createToken(userEmail, userNumber, roles, accessTokenValidity);
    }

    // Refresh Token 생성
    public String createRefreshToken(String userEmail, Integer userNumber) {
        return createToken(userEmail,userNumber, null, refreshTokenValidity);
    }

    // 공통적으로 토큰 생성하는 로직
    public String createToken(String userEmail, Integer userNumber, List<String> roles, long validityInMilliseconds) {
        Claims claims = Jwts.claims().setSubject(userEmail);
        claims.put("userNumber", userNumber);
        if (roles != null &&!roles.isEmpty()) {
            claims.put("roles", roles);
        }

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        try {
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(validity)
                    .signWith(SignatureAlgorithm.HS256, secretKey)
                    .compact();
            log.info("JWT 토큰 생성 성공: userEmail={}, userNumber={}", userEmail, userNumber);
            return token;
        } catch (Exception e) {
            log.error("JWT 토큰 생성 중 오류 발생: userEmail={}, userNumber={}", userEmail, userNumber, e);
            throw new JwtException("JWT 토큰 생성에 실패했습니다.", e);
        }
    }

    public boolean validateToken(String token) {

        if (jwtBlacklistService.isTokenBlacklisted(token)) {
            log.warn("블랙리스트에 등록된 토큰 검증 시도: {}", token);
            return false; // 블랙리스트에 있으면 토큰을 무효화
        }
        // JWT 가 유효한지 검증
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            log.info("JWT 토큰 유효성 검증 성공: {}", token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않거나 만료된 JWT 토큰: {}", token, e);
            return false;
        }
    }
    // JWT에서 사용자 이메일을 추출
    public String getUserEmail(String token) {
        try {
            String email = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
            log.info("JWT 토큰에서 사용자 이메일 추출 성공: {}", email);
            return email;
        } catch (JwtException e) {
            log.error("JWT 토큰에서 사용자 이메일 추출 실패: {}", token, e);
            throw new JwtException("JWT 토큰에서 사용자 이메일을 추출하는데 실패했습니다.", e);
        }
    }
    // JWT에서 사용자 ID 추출
    public Integer getUserNumber(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            Integer userNumber = claims.get("userNumber", Integer.class);
            log.info("JWT 토큰에서 사용자 ID 추출 성공: userNumber={}", userNumber);
            return userNumber;
        } catch (JwtException e) {
            log.error("JWT 토큰에서 사용자 ID 추출 실패: {}", token, e);
            throw new JwtException("JWT 토큰에서 사용자 ID를 추출하는데 실패했습니다.", e);
        }
    }

    // Refresh Token이 유효하다면 새로운 Access Token 발급
    public String refreshAccessToken(String refreshToken) {
        if (validateToken(refreshToken)) {
            try {
                String userEmail = getUserEmail(refreshToken);
                Integer userNumber = getUserNumber(refreshToken);
                String newAccessToken = createAccessToken(userEmail, userNumber, null);
                log.info("새로운 Access Token 발급 성공: userEmail={}, userNumber={}", userEmail, userNumber);
                return newAccessToken;
            } catch (Exception e) {
                log.error("Access Token 발급 중 오류 발생: refreshToken={}", refreshToken, e);
                throw new JwtException("새로운 Access Token 발급에 실패했습니다.", e);
            }
        } else {
            log.warn("유효하지 않은 Refresh Token 사용 시도: {}", refreshToken);
            throw new JwtException("유효하지 않은 Refresh Token입니다.");
        }
    }
    // JWT 토큰의 만료 시간을 추출하는 메서드 추가
    public long getExpiration(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            Date expiration = claims.getExpiration();
            log.info("JWT 토큰의 만료 시간 추출 성공: expiration={}", expiration);
            return expiration.getTime();
        } catch (JwtException e) {
            log.error("JWT 토큰의 만료 시간 추출 실패: {}", token, e);
            throw new JwtException("JWT 토큰에서 만료 시간을 추출하는데 실패했습니다.", e);
        }
    }
}
