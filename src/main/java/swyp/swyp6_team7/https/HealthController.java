package swyp.swyp6_team7.https;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.global.utils.api.ApiResponse;

@RestController
public class HealthController {

    @GetMapping("/actuator/health")
    public ApiResponse<String> healthCheck() {
        String responseBody = "Healthy";
        return ApiResponse.success(responseBody); // 200 OK 응답
    }
}