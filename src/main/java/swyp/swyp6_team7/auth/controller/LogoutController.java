package swyp.swyp6_team7.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.auth.service.LogoutService;
import swyp.swyp6_team7.global.utils.api.ApiResponse;


@Slf4j
@RestController
@RequiredArgsConstructor
public class LogoutController {

    private final LogoutService logoutService;


    @PostMapping("/api/logout")
    public ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {
        logoutService.logout(request, response);

        return ApiResponse.success("로그아웃 성공");
    }
}
