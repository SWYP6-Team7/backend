package swyp.swyp6_team7.member.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.config.TestConfig;
import swyp.swyp6_team7.member.dto.UserRequestDto;
import swyp.swyp6_team7.member.entity.*;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.profile.service.ProfileService;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.repository.UserTagPreferenceRepository;
import swyp.swyp6_team7.tag.service.TagService;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "kakao.client-id=fake-client-id",
        "kakao.client-secret=fake-client-secret",
        "kakao.redirect-uri=http://localhost:8080/login/oauth2/code/kakao",
        "kakao.token-url=https://kauth.kakao.com/oauth/token",
        "kakao.user-info-url=https://kapi.kakao.com/v2/user/me"
})
public class UserServiceTest {

    @Autowired
    private MemberService memberService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TagService tagService;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private UserTagPreferenceRepository userTagPreferenceRepository;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    public void testSignUpSeccess(){
        // given
        UserRequestDto userRequestDto = new UserRequestDto(
                "test@example.com",
                "password",
                "testuser",
                "M",  // String 타입으로 성별 전달
                "20대",  // String 타입으로 나이 그룹 전달
                Set.of("국내", "가성비")  // 선호 태그
        );

        // 암호화된 비밀번호 설정
        Mockito.when(passwordEncoder.encode(userRequestDto.getPassword())).thenReturn("encodedPassword");

        // 이메일 중복 체크에서 빈 Optional 반환
        Mockito.when(userRepository.findByUserEmail(userRequestDto.getEmail())).thenReturn(Optional.empty());

        // 태그 처리
        Mockito.when(tagService.createTags(userRequestDto.getPreferredTags()))
                .thenReturn(Set.of(Tag.of("국내"), Tag.of("가성비")));

        // 새로운 유저 설정
        Users newUser = Users.builder()
                .userEmail(userRequestDto.getEmail())
                .userPw("encodedPassword")
                .userName(userRequestDto.getName())
                .userGender(Gender.M)
                .userAgeGroup(AgeGroup.TWENTY)
                .userStatus(UserStatus.ABLE)
                .role(UserRole.USER)
                .build();

        // Mock: userRepository.save()가 유저 객체를 반환하도록 설정
        newUser.setUserNumber(1);  // userNumber 설정
        Mockito.when(userRepository.save(Mockito.any(Users.class))).thenAnswer(invocation -> {
            Users savedUser = invocation.getArgument(0);  // 저장하려는 유저를 가져옴
            savedUser.setUserNumber(1);  // userNumber 설정
            return savedUser;
        });

        // JWT 발급 시 반환할 값 설정
        Mockito.when(jwtProvider.createToken(Mockito.anyInt(), Mockito.anyList(), Mockito.anyLong()))
                .thenReturn("jwt-token");

        // when
        Map<String, Object> response = memberService.signUp(userRequestDto);

        // then
        assertEquals("test@example.com", response.get("email"));
        assertNotNull(response.get("userNumber"));  // userNumber가 존재하는지 확인
        assertEquals("jwt-token", response.get("accessToken"));  // 토큰이 올바르게 생성되었는지 확인

    }
    @Test
    public void testSignUpEmailAlreadyExists() {
        // given
        String email = "test@example.com";
        UserRequestDto userRequestDto = new UserRequestDto(
                email,
                "password",
                "testuser",
                "M",
                "TWENTY",
                Set.of("국내", "가성비")
        );

        // 이미 존재하는 사용자
        Mockito.when(userRepository.findByUserEmail(email)).thenReturn(Optional.of(new Users()));

        // when & then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            memberService.signUp(userRequestDto);
        });
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("이미 사용 중인 이메일입니다.", exception.getReason());
    }
}
