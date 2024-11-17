package swyp.swyp6_team7.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.dto.UserRequestDto;
import swyp.swyp6_team7.member.entity.*;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.profile.service.ProfileService;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.domain.UserTagPreference;
import swyp.swyp6_team7.tag.repository.UserTagPreferenceRepository;
import swyp.swyp6_team7.tag.service.TagService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final MemberDeletedService memberDeletedService;
    private final TagService tagService;
    private final UserTagPreferenceRepository userTagPreferenceRepository;

    @Value("${custom.admin-secret-key}")
    private String adminSecretKey;


    @Transactional(readOnly = true)
    public Users findByUserNumber(Integer userNumber) {
        log.info("사용자 번호로 사용자 찾기 요청: userNumber={}", userNumber);
        return userRepository.findById(userNumber)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음: userNumber={}", userNumber);
                    return new IllegalArgumentException("존재하지 않는 사용자입니다.");
                });
    }

    public Map<String, Object> signUp(UserRequestDto userRequestDto) {
        log.info("회원가입 요청: email={}", userRequestDto.getEmail());
        try {
            // 이메일 중복 체크
            if (userRepository.findByUserEmail(userRequestDto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }

            // 태그 개수 검증 - 최대 5개까지 허용
            if (userRequestDto.getPreferredTags().size() > 5) {
                throw new IllegalArgumentException("태그는 최대 5개까지만 선택할 수 있습니다.");
            }

            // Argon2로 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(userRequestDto.getPassword());

            // 성별 ENUM 변환
            Gender gender = Gender.valueOf(userRequestDto.getGender().toUpperCase());

            // 연령대 ENUM 변환 및 검증
            AgeGroup ageGroup;
            try {
                ageGroup = AgeGroup.fromValue(userRequestDto.getAgegroup());

            } catch (IllegalArgumentException e) {
                log.error("잘못된 연령대 값 제공: ageGroup={}", userRequestDto.getAgegroup(), e);
                throw new IllegalArgumentException("Invalid age group provided.");
            }

            // Users 객체에 암호화된 비밀번호 설정
            Users newUser = Users.builder()
                    .userEmail(userRequestDto.getEmail())
                    .userPw(encodedPassword)
                    .userName(userRequestDto.getName())
                    .userGender(gender)
                    .userAgeGroup(ageGroup)
                    .role(UserRole.USER)
                    .userStatus(UserStatus.ABLE)
                    .preferredTags(tagService.createTags(userRequestDto.getPreferredTags())) // 태그 처리
                    .build();
            userRepository.save(newUser);
            log.info("회원가입 성공: userNumber={}", newUser.getUserNumber());


            // 선호 태그 연결 로직
            if (userRequestDto.getPreferredTags() != null && !userRequestDto.getPreferredTags().isEmpty()) {
                List<UserTagPreference> tagPreferences = userRequestDto.getPreferredTags().stream().map(tagName -> {
                    Tag tag = tagService.findByName(tagName); // 태그가 없으면 생성
                    UserTagPreference userTagPreference = new UserTagPreference();
                    userTagPreference.setUser(newUser);
                    userTagPreference.setTag(tag);
                    return userTagPreference;
                }).collect(Collectors.toList());

                // 선호 태그 저장
                userTagPreferenceRepository.saveAll(tagPreferences);
            }

            // JWT 발급
            String token = jwtProvider.createToken(newUser.getUserNumber(), List.of(newUser.getRole().name()), 3600000);

            // 응답 데이터에 userId와 accessToken 포함
            Map<String, Object> response = new HashMap<>();
            response.put("userNumber", newUser.getUserNumber());
            response.put("email", newUser.getUserEmail());
            response.put("accessToken", token);

            return response;
        } catch (IllegalArgumentException e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            log.error("회원가입 중 알 수 없는 오류 발생: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 인해 회원가입에 실패했습니다.");
        }
    }

    // 관리자 생성 메서드
    @Transactional
    public Map<String, Object> createAdmin(UserRequestDto userRequestDto) {
        // adminSecretKey 확인
        if (!userRequestDto.getAdminSecretKey().equals(adminSecretKey)) {
            throw new IllegalArgumentException("잘못된 관리자 시크릿 키입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userRequestDto.getPassword());


        // 성별 ENUM 변환
        Gender gender = Gender.valueOf(userRequestDto.getGender().toUpperCase());

        // 연령대 ENUM 변환 및 검증
        AgeGroup ageGroup;
        try {
            ageGroup = AgeGroup.valueOf(userRequestDto.getAgegroup().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid age group provided.");
        }

        // 관리자 상태 및 역할 설정
        UserStatus status = UserStatus.ABLE;


        // 새로운 관리자 생성
        Users newAdmin = Users.builder()
                .userEmail(userRequestDto.getEmail())
                .userPw(encodedPassword)
                .userName(userRequestDto.getName())
                .userGender(gender)
                .userAgeGroup(ageGroup)
                .role(UserRole.ADMIN)
                .userStatus(status)
                .build();

        // 관리자 저장
        userRepository.save(newAdmin);

        // 권한을 String으로 변환하여 리스트로 만들기
        List<String> roles = newAdmin.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // JWT 발급
        long tokenExpirationTime = 3600000; // 토큰 만료 시간 1시간
        String token = jwtProvider.createToken(newAdmin.getUserNumber(), roles, tokenExpirationTime);

        // 응답 데이터
        Map<String, Object> response = new HashMap<>();
        response.put("userNumber", newAdmin.getUserNumber());
        response.put("email", newAdmin.getUserEmail());
        response.put("accessToken", token);

        return response;

    }

    // 이메일 중복 확인 로직
    public boolean checkEmailDuplicate(String email) {
        if (userRepository.findByUserEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        return false; // 중복된 이메일이 없을 경우 false반환
    }

    @Transactional
    public void updateLoginDate(Users user) {
        user.setUserLoginDate(LocalDateTime.now());  // 현재 시간을 로그인 시간으로 설정
        userRepository.save(user);  // 업데이트된 사용자 정보 저장
    }

    @Transactional
    public void updateLogoutDate(Users user) {
        user.setUserLogoutDate(LocalDateTime.now());  // 현재 시간을 로그아웃 시간으로 설정
        userRepository.save(user);  // 업데이트된 사용자 정보 저장
    }

    public Users getUserByEmail(String email) {
        return userRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다: " + email));
    }

    private Users findUserById(Integer userNumber) {
        return userRepository.findById(userNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }
}
