package swyp.swyp6_team7.auth.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;

import java.util.Base64;
import java.util.List;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtProvider {
    private final byte[] secretKey;
    private final long accessTokenValidity = 15 * 60 * 1000; // 15분
    private final long refreshTokenValidity = 7 * 24 * 60 * 60 * 1000; // 1주일
    private final JwtBlacklistService jwtBlacklistService;

    public JwtProvider(@Value("${custom.jwt.secretKey}") String secretKey, JwtBlacklistService jwtBlacklistService) {
        this.secretKey = Base64.getDecoder().decode(secretKey);
        this.jwtBlacklistService = jwtBlacklistService;
    }

    // Access Token 생성
    public String createAccessToken(Integer userNumber, List<String> roles) {
        return createToken(userNumber, roles, accessTokenValidity);
    }

    // Refresh Token 생성
    public String createRefreshToken(Integer userNumber) {
        return createToken(userNumber, null, refreshTokenValidity);
    }

    // 공통적으로 토큰 생성하는 로직
    public String createToken(Integer userNumber, List<String> roles, long validityInMilliseconds) {
        Claims claims = Jwts.claims();
        claims.put("userNumber", userNumber);
        if (roles != null && !roles.isEmpty()) {
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
            log.info("JWT 토큰 생성 성공: userNumber={}", userNumber);
            return token;
        } catch (Exception e) {
            log.error("JWT 토큰 생성 중 오류 발생: userNumber={}", userNumber, e);
            throw new JwtException("JWT 토큰 생성에 실패했습니다.", e);
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("토큰이 null이거나 비어 있습니다.");
            return false;
        }
        if (jwtBlacklistService.isTokenBlacklisted(token)) {
            log.warn("블랙리스트에 등록된 토큰 검증 시도: {}", token);
            throw new JwtException("블랙리스트에 등록된 토큰입니다.");
        }
        // JWT 가 유효한지 검증
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            log.info("JWT 토큰 유효성 검증 성공: {}", token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", token, e);
            return false;
        }
    }

    // JWT에서 사용자 ID 추출
    public Integer getUserNumber(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            Integer userNumber = claims.get("userNumber", Integer.class);
            log.info("JWT 토큰에서 사용자 ID 추출 성공: userNumber={}", userNumber);
            return userNumber;
        } catch (ExpiredJwtException e) {
            Integer userNumber = e.getClaims().get("userNumber", Integer.class);
            log.warn("만료된 JWT 토큰에서 사용자 ID 추출: userNumber={}", userNumber);
            return userNumber;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
            throw new JwtException("JWT 토큰에서 사용자 ID를 추출하는데 실패했습니다.", e);
        }
    }

    // Refresh Token이 유효하다면 새로운 Access Token 발급
    public String refreshAccessToken(String refreshToken) {

        try {
            if (!validateToken(refreshToken)) {
                log.warn("유효하지 않은 Refresh Token 사용 시도");
                throw new JwtException("유효하지 않은 Refresh Token입니다.");
            }
        } catch (ExpiredJwtException e) {
            log.warn("만료된 Refresh Token 사용 시도");
            throw new JwtException("만료된 Refresh Token입니다.");
        }
        try {
            Integer userNumber = getUserNumber(refreshToken);
            String newAccessToken = createAccessToken(userNumber, null);
            log.info("새로운 Access Token 발급 성공: userNumber={}", userNumber);
            return newAccessToken;
        } catch (Exception e) {
            log.error("Access Token 발급 중 오류 발생: refreshToken={}", refreshToken, e);
            throw new JwtException("새로운 Access Token 발급에 실패했습니다.", e);
        }
    }

    // JWT 토큰의 만료 시간을 추출하는 메서드 추가
    public long getExpiration(String token) {
        try {
            // JWT 토큰 파싱 및 클레임 추출
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            if (expiration == null) {
                log.error("JWT 토큰에서 만료 시간을 찾을 수 없음: token={}", token);
                throw new JwtException("JWT 토큰에서 만료 시간을 찾을 수 없습니다.");
            }

            log.info("JWT 토큰의 만료 시간 추출 성공: expiration={}", expiration);
            return TimeUnit.MILLISECONDS.toSeconds(expiration.getTime()); // 초 단위로 반환
        } catch (JwtException e) {
            log.error("JWT 토큰의 만료 시간 추출 실패: {}", token, e);
            throw new JwtException("JWT 토큰에서 만료 시간을 추출하는데 실패했습니다.", e);
        }
    }

    public long getRemainingValidity(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        Date expiration = claims.getExpiration();
        long currentTime = System.currentTimeMillis();
        return (expiration.getTime() - currentTime) / 1000;
    }

}
