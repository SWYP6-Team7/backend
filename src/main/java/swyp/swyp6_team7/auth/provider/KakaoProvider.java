package swyp.swyp6_team7.auth.provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class KakaoProvider implements SocialLoginProvider {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")

    private String redirectUri;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public KakaoProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(String provider) {
        return "kakao".equalsIgnoreCase(provider);  // 카카오 제공자를 식별
    }


    public Map<String, String> getUserInfoFromKakao(String code) {
        log.info("Kakao 사용자 정보 요청: code={}", code);

        try {
            String accessToken = getAccessToken(code);
            log.info("Kakao Access Token 획득 성공");

            String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);

                Map<String, Object> kakaoAccount = (Map<String, Object>) result.get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

                String socialLoginId = String.valueOf(result.get("id"));
                String nickname = (String) profile.get("nickname");

                log.info("Kakao 사용자 정보 파싱 성공: socialLoginId={}, nickname={}", socialLoginId, nickname);
                return Map.of(
                        "socialLoginId", socialLoginId,
                        "nickname", nickname
                );
            } else {
                log.warn("Kakao 사용자 정보 요청 실패: status={}", response.getStatusCode());
                throw new RuntimeException("Failed to get user info from Kakao");
            }
        } catch (Exception e) {
            log.error("Kakao 사용자 정보 파싱 중 오류 발생", e);
            throw new RuntimeException("Failed to parse user info from Kakao", e);
        }
    }

    private String getAccessToken(String code) {
        log.info("Kakao Access Token 요청: code={}", code);

        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId); // 카카오 앱 REST API 키
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
                String accessToken = (String) result.get("access_token");
                log.info("Kakao Access Token 파싱 성공");
                return accessToken;
            } catch (Exception e) {
                log.error("Kakao Access Token 파싱 중 오류 발생", e);
                throw new RuntimeException("Failed to parse access token from Kakao", e);
            }
        } else {
            log.warn("Kakao Access Token 요청 실패: status={}", response.getStatusCode());
            throw new RuntimeException("Failed to get access token");
        }
    }

    private String getAgeGroup(String ageRange) {
        if (ageRange != null) {
            if (ageRange.startsWith("10")) return "10대";
            else if (ageRange.startsWith("20")) return "20대";
            else if (ageRange.startsWith("30")) return "30대";
            else if (ageRange.startsWith("40")) return "40대";
        }
        return "50대 이상";
    }
}
