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

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class NaverProvider implements SocialLoginProvider{

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public NaverProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(String provider) {
        return "naver".equalsIgnoreCase(provider);  // 네이버 제공자를 식별
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getClientId() {
        return clientId;
    }


    public Map<String, String> getUserInfoFromNaver(String code, String state) {
        log.info("Naver 사용자 정보 요청: code={}, state={}", code, state);

        try {
            String accessToken = getAccessToken(code, state);
            log.info("Naver Access Token 획득 성공");

            String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
                Map<String, Object> responseBody = (Map<String, Object>) result.get("response");

                // 사용자 정보를 Map으로 변환하여 반환
                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("email", (String) responseBody.get("email"));
                userInfo.put("name", (String) responseBody.get("name"));
                userInfo.put("gender", (String) responseBody.get("gender"));
                userInfo.put("ageGroup", getAgeGroup((String) responseBody.get("age")));
                userInfo.put("socialID", (String) responseBody.get("id"));
                userInfo.put("provider", "naver");

                log.info("Naver 사용자 정보 파싱 성공: userInfo={}", userInfo);
                return userInfo;
            } else {
                log.warn("Naver 사용자 정보 요청 실패: status={}", response.getStatusCode());
                throw new RuntimeException("Failed to get user info from Naver");
            }
        } catch (Exception e) {
            log.error("Naver 사용자 정보 파싱 중 오류 발생", e);
            throw new RuntimeException("Failed to parse user info from Naver", e);
        }
    }

    private String getAccessToken(String code, String state) {
        log.info("Naver Access Token 요청: code={}, state={}", code, state);

        String tokenUrl = "https://nid.naver.com/oauth2.0/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", state);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
                String accessToken = (String) result.get("access_token");
                log.info("Naver Access Token 파싱 성공");
                return accessToken;
            } catch (Exception e) {
                log.error("Naver Access Token 파싱 중 오류 발생", e);
                throw new RuntimeException("Failed to parse access token from Naver", e);
            }
        } else {
            log.warn("Naver Access Token 요청 실패: status={}", response.getStatusCode());
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
