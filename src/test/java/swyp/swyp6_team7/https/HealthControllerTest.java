package swyp.swyp6_team7.https;

import org.springframework.test.context.TestPropertySource;
import swyp.swyp6_team7.global.IntegrationTest;

@TestPropertySource(properties = {
        "kakao.client-id=fake-client-id",
        "kakao.client-secret=fake-client-secret",
        "kakao.redirect-uri=http://localhost:8080/login/oauth2/code/kakao",
        "kakao.token-url=https://kauth.kakao.com/oauth/token",
        "kakao.user-info-url=https://kapi.kakao.com/v2/user/me"
})
public class HealthControllerTest extends IntegrationTest {
}