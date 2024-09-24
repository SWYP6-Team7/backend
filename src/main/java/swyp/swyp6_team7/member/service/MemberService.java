package swyp.swyp6_team7.member.service;

import io.jsonwebtoken.Jwt;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.dto.UserRequestDto;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import swyp.swyp6_team7.profile.dto.ProfileCreateRequest;
import swyp.swyp6_team7.profile.service.ProfileService;

import org.springframework.security.core.GrantedAuthority;
import swyp.swyp6_team7.tag.service.TagService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    private final ProfileService profileService;
    private final TagService tagService;

    private final String adminSecretKey = "tZ37HBGNyfUZVzgXGiv1OEBHvmgCyVB7";


    @Autowired

    public MemberService(UserRepository userRepository, PasswordEncoder passwordEncoder, ProfileService profileService, @Lazy JwtProvider jwtProvider, TagService tagService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.profileService = profileService;
        this.tagService = tagService;
    }

    @Transactional(readOnly = true)
    public Users findByEmail(String email) {
        return userRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    @Transactional(readOnly = true)
    public List<String> findPreferredTagsByEmail(String email) {
        Users user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getPreferredTags().isEmpty()) {
            throw new IllegalArgumentException("사용자 태그가 설정되어 있지 않습니다.");
        }

        return user.getPreferredTags().stream()
                .map(tag -> tag.getName())
                .toList();
    }

    public Map<String, Object> signUp(UserRequestDto userRequestDto) {

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
        Users.Gender gender = Users.Gender.valueOf(userRequestDto.getGender().toUpperCase());

        // 연령대 ENUM 변환 및 검증
        Users.AgeGroup ageGroup;
        try {
            ageGroup = Users.AgeGroup.fromValue(userRequestDto.getAgegroup());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid age group provided.");
        }

        // 기본 상태를 ABLE로 설정 (회원 상태 ENUM 사용)
        Users.MemberStatus status = Users.MemberStatus.ABLE;

        Users.UserRole role = Users.UserRole.USER;


        // Users 객체에 암호화된 비밀번호 설정
        Users newUser = Users.builder()
                .userEmail(userRequestDto.getEmail())
                .userPw(encodedPassword)  // 암호화된 비밀번호 설정
                .userName(userRequestDto.getName())
                .userGender(gender)
                .userAgeGroup(ageGroup)
                .role(role) // 기본 역할 설정
                .userStatus(status)  // 기본 사용자 상태 설정
                .preferredTags(tagService.createTags(userRequestDto.getPreferredTags())) // 태그 처리
                .build();


        // 사용자 저장
        userRepository.save(newUser);


        // 프로필 생성 요청
        ProfileCreateRequest profileCreateRequest = new ProfileCreateRequest();
        profileCreateRequest.setUserNumber(newUser.getUserNumber());
        profileService.createProfile(profileCreateRequest);

        // 역할을 리스트로 변환하여 JWT 생성 시 전달
        List<String> roles = List.of(newUser.getRole().name());  // ENUM을 String으로 변환하여 List로 만들기


        // JWT 발급
        long tokenExpirationTime = 3600000; // 토큰 만료 시간 추가(1시간)
        String token = jwtProvider.createToken(newUser.getEmail(), roles, tokenExpirationTime);

        // 응답 데이터에 userId와 accessToken 포함
        Map<String, Object> response = new HashMap<>();
        response.put("userNumber", newUser.getUserNumber());
        response.put("email", newUser.getUserEmail());
        response.put("accessToken", token);

        return response;
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
        Users.Gender gender = Users.Gender.valueOf(userRequestDto.getGender().toUpperCase());

        // 연령대 ENUM 변환 및 검증
        Users.AgeGroup ageGroup;
        try {
            ageGroup = Users.AgeGroup.valueOf(userRequestDto.getAgegroup().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid age group provided.");
        }

        // 관리자 상태 및 역할 설정
        Users.MemberStatus status = Users.MemberStatus.ABLE;


        // 새로운 관리자 생성
        Users newAdmin = Users.builder()
                .userEmail(userRequestDto.getEmail())
                .userPw(encodedPassword)
                .userName(userRequestDto.getName())
                .userGender(gender)
                .userAgeGroup(ageGroup)
                .role(Users.UserRole.ADMIN)
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
        String token = jwtProvider.createToken(newAdmin.getUserEmail(), roles, tokenExpirationTime);

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
}
