package swyp.swyp6_team7.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import swyp.swyp6_team7.auth.dto.SocialUserDTO;
import swyp.swyp6_team7.auth.service.NaverService;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

@RestController
public class NaverController {

    private final NaverService naverService;

    public NaverController(NaverService naverService) {
        this.naverService = naverService;
    }

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    // 네이버 로그인 리다이렉트 URL
    @GetMapping("/login/oauth/naver")
    public ResponseEntity<Void> naverLoginRedirect(HttpServletResponse response, HttpSession session) throws IOException {
        String state = UUID.randomUUID().toString(); // CSRF 방지용 state 값
        session.setAttribute("oauth_state", state); // 세션에 state 값 저장

        String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + redirectUri
                + "&state=" + state;

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(naverAuthUrl));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    // 네이버 콜백 처리
    @GetMapping("/login/oauth/naver/callback")
    public ResponseEntity<?> naverCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpSession session) {


        // 세션에서 저장한 state 값 가져와서 비교
        String sessionState = (String) session.getAttribute("oauth_state");
        if (sessionState == null || !sessionState.equals(state)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid state parameter");
        }
        // 중복된 code 값 사용 방지
        if (session.getAttribute("oauth_code") != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authorization code already used");
        }
        session.setAttribute("oauth_code", code);  // code 값 저장하여 중복 사용 방지

        try {
            Map<String, String> userInfo = naverService.processNaverLogin(code, state);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to process Naver login: " + e.getMessage());
        }
    }

}
