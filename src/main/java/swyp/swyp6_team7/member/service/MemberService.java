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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MemberService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Autowired
    public MemberService(UserRepository userRepository, PasswordEncoder passwordEncoder, @Lazy JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Transactional(readOnly = true)
    public Users findByEmail(String email) {
        return userRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    public Map<String, Object> signUp(UserRequestDto userRequestDto) {

        // Argon2로 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userRequestDto.getPassword());

        // 전화번호 포맷팅 (000-0000-0000 형식으로 변환)
        String formattedPhoneNumber = formatPhoneNumber(userRequestDto.getPhone());

        // 성별 ENUM 변환
        Users.Gender gender = Users.Gender.valueOf(userRequestDto.getGender().toUpperCase());

        // 기본 상태를 ABLE로 설정 (회원 상태 ENUM 사용)
        Users.MemberStatus status = Users.MemberStatus.ABLE;

        // 성별 변환
        Users.Gender convertedgender = Users.Gender.valueOf(userRequestDto.getGender().toUpperCase());
        // Users 객체에 암호화된 비밀번호 설정
        Users newUser = Users.builder()
                .userEmail(userRequestDto.getEmail())
                .userPw(encodedPassword)  // 암호화된 비밀번호 설정
                .userName(userRequestDto.getName())
                .userPhone(formattedPhoneNumber)
                .userGender(convertedgender)
                .userBirthYear(userRequestDto.getBirthYear())
                .roles(List.of("ROLE_USER"))  // 기본 역할 설정
                .userStatus(status)  // 기본 사용자 상태 설정
                .build();
        newUser.setPassword(encodedPassword);

        // 사용자 저장
        userRepository.save(newUser);

        // JWT 발급
        long tokenExpirationTime = 3600000; // 토큰 만료 시간 추가(1시간)
        String token = jwtProvider.createToken(newUser.getEmail(), newUser.getRoles(), tokenExpirationTime);

        // 응답 데이터에 userId와 accessToken 포함
        Map<String, Object> response = new HashMap<>();
        response.put("userNumber", newUser.getUserNumber());
        response.put("email", newUser.getUserEmail());
        response.put("accessToken", token);

        return response;
    }

    public String formatPhoneNumber(String phoneNumber) {
        // 숫자만 남기고 000-0000-0000 형식으로 변환
        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
        return phoneNumber.replaceFirst("(\\d{3})(\\d{4})(\\d+)", "$1-$2-$3");
    }

    // 이메일 중복 확인 로직
    public boolean checkEmailDuplicate(String email) {
        if (userRepository.findByUserEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        return false; // 중복된 이메일이 없을 경우 false반환
    }
}