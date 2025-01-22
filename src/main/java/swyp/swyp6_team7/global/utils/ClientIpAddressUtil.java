package swyp.swyp6_team7.global.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public class ClientIpAddressUtil {

    private static final List<String> IP_HEADERS = List.of(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "REMOTE_ADDR"
    );

    public static String getClientIp(HttpServletRequest request) {
        String clientIp;

        // 헤더 목록에서 Client IP 확인
        for (String ipHeader : IP_HEADERS) {
            clientIp = request.getHeader(ipHeader);
            if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
                continue;
            }
            return clientIp;
        }

        // IP를 찾지 못한 경우
        return request.getRemoteAddr();
    }
}
