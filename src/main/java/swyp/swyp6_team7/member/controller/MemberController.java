package swyp.swyp6_team7.member.controller;

import org.springframework.http.ResponseEntity;
import swyp.swyp6_team7.member.dto.UserRequestDto;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/new")
    public ResponseEntity<String> signUp(@RequestBody UserRequestDto userRequestDto) {
        // DTO 객체를 사용하여 회원 가입 처리
        Map<String, Object> response = memberService.signUp(userRequestDto);
        return ResponseEntity.ok("User registered successfully");
    }
}
