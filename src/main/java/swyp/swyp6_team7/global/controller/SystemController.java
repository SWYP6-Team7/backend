package swyp.swyp6_team7.global.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.global.utils.api.ApiResponse;

@RestController
@Slf4j
public class SystemController {

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("PONG");
    }
}
