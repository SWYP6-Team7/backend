package swyp.swyp6_team7.auth.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import swyp.swyp6_team7.auth.dto.SocialUserDTO;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class GoogleProvider implements SocialLoginProvider {
    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")

    private String redirectUri;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(String provider) {
        return "google".equalsIgnoreCase(provider);  // 카카오 제공자를 식별
    }


    public Map<String, String> getUserInfoFromGoogle(String code) {
        log.info("Google 사용자 정보 요청: code={}", code);

        try {
            String accessToken = getAccessToken(code);
            log.info("Google Access Token 획득 성공");

            String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);

                String socialLoginId = String.valueOf(result.get("sub")); // Google 고유 ID
                String email = (String) result.get("email");
                String name = (String) result.get("name");

                log.info("Google 사용자 정보 파싱 성공: socialLoginId={}, email={}, name={}", socialLoginId, email, name);
                return Map.of(
                        "socialLoginId", socialLoginId,
                        "email", email,
                        "name", name
                );
            } else {
                log.warn("Google 사용자 정보 요청 실패: status={}", response.getStatusCode());
                throw new RuntimeException("Failed to get user info from Google");
            }
        } catch (Exception e) {
            log.error("Google 사용자 정보 파싱 중 오류 발생", e);
            throw new RuntimeException("Failed to parse user info from Google", e);
        }
    }

    private String getAccessToken(String code) {
        log.info("Google Access Token 요청: code={}",code);

        String tokenUrl = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
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
                log.info("Google Access Token 파싱 성공");
                return accessToken;
            } catch (Exception e) {
                log.error("Google Access Token 파싱 중 오류 발생", e);
                throw new RuntimeException("Failed to parse access token", e);
            }
        } else {
            log.warn("Google Access Token 요청 실패: status={}", response.getStatusCode());
            throw new RuntimeException("Failed to get access token from Google");
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