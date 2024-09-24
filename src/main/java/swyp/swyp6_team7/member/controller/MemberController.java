package swyp.swyp6_team7.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import swyp.swyp6_team7.member.dto.UserRequestDto;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;
    private final UserLoginHistoryService userLoginHistoryService;

    public MemberController(MemberService memberService,UserLoginHistoryService userLoginHistoryService) {
        this.memberService = memberService;
        this.userLoginHistoryService = userLoginHistoryService;
    }

    @PostMapping("/users/new")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody UserRequestDto userRequestDto) {
        // DTO 객체를 사용하여 회원 가입 처리
        try {
            Map<String, Object> response = memberService.signUp(userRequestDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",e.getMessage()));  // 409 Conflict 반환
        }
    }
    // 이메일 중복 확인 엔드포인트
    @GetMapping("/users-email")
    public ResponseEntity<String> checkEmailDuplicate(@RequestParam("email") String email) {
        try {
            memberService.checkEmailDuplicate(email);  // 이메일 중복 확인 서비스 호출
            return ResponseEntity.ok("사용 가능한 이메일입니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());  // 이미 사용 중인 이메일
        }
    }
    // 관리자 회원 가입
    @PostMapping("/admins/new")
    public ResponseEntity<?> createAdmin(@RequestBody UserRequestDto userRequestDto){
        memberService.createAdmin(userRequestDto);
        return new ResponseEntity<>("Admin successfully registered", HttpStatus.CREATED);
    }

}
