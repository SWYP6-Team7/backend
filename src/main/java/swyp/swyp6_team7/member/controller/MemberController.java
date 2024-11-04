package swyp.swyp6_team7.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.dto.UserRequestDto;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberDeletedService;
import swyp.swyp6_team7.member.service.MemberService;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class MemberController {
    private final Logger logger = LoggerFactory.getLogger(MemberController.class);

    private final MemberService memberService;
    private final MemberDeletedService memberDeletedService;
    private final UserLoginHistoryService userLoginHistoryService;
    private final JwtProvider jwtProvider;

    public MemberController(MemberService memberService,
                            UserLoginHistoryService userLoginHistoryService,
                            JwtProvider jwtProvider,
                            MemberDeletedService memberDeletedService) {
        this.memberService = memberService;
        this.userLoginHistoryService = userLoginHistoryService;
        this.jwtProvider =jwtProvider;
        this.memberDeletedService = memberDeletedService;
    }

    @PostMapping("/users/new")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody UserRequestDto userRequestDto) {
        logger.info("회원 가입 요청: {}", userRequestDto.getEmail());
        // DTO 객체를 사용하여 회원 가입 처리
        try {
            Map<String, Object> response = memberService.signUp(userRequestDto);
            logger.info("회원 가입 성공: {}", userRequestDto.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("회원 가입 실패 - 중복된 이메일: {}",userRequestDto.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",e.getMessage()));  // 409 Conflict 반환
        }
    }
    // 이메일 중복 확인 엔드포인트
    @GetMapping("/users-email")
    public ResponseEntity<String> checkEmailDuplicate(@RequestParam("email") String email) {
        logger.info("이메일 중복 확인 요청: {}",email);
        try {
            memberDeletedService.validateReRegistration(email);
            memberService.checkEmailDuplicate(email);
            logger.info("이메일 사용 가능: {}",email);
            return ResponseEntity.ok("사용 가능한 이메일입니다.");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("3개월")) {
                logger.warn("재가입 제한: {} - 3개월 내에 재가입 불가", email);
            } else {
                logger.warn("이메일 중복: {} - 이미 사용 중인 이메일", email);
            }

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // 관리자 회원 가입
    @PostMapping("/admins/new")
    public ResponseEntity<?> createAdmin(@RequestBody UserRequestDto userRequestDto){
        logger.info("관리자 회원 가입 요청: {}", userRequestDto.getEmail());
        memberService.createAdmin(userRequestDto);
        logger.info("관리자 회원 가입 성공: {}", userRequestDto.getEmail());
        return new ResponseEntity<>("Admin successfully registered", HttpStatus.CREATED);
    }
    // 회원 탈퇴
    @DeleteMapping("/user/delete")
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String token) {
        logger.info("회원 탈퇴 요청");
        try {
            String jwtToken = token.replace("Bearer ", "");
            Integer userNumber = jwtProvider.getUserNumber(jwtToken);
            logger.info("회원 번호 추출 완료: {}", userNumber);

            // 회원 탈퇴 서비스 호출
            memberService.deleteUser(userNumber);
            logger.info("회원 탈퇴 성공: 회원 번호 {}",userNumber);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (IllegalArgumentException e) {
            logger.error("회원 탈퇴 실패 - 회원을 찾을 수 없음");
            return ResponseEntity.notFound().build(); // 404 Not Found
        } catch (Exception e) {
            logger.error("회원 탈퇴 실패 - 서버 오류",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }
}
