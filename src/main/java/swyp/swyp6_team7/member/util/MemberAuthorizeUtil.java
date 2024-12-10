package swyp.swyp6_team7.member.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import swyp.swyp6_team7.auth.details.CustomUserDetails;


public class MemberAuthorizeUtil {

    private MemberAuthorizeUtil() {
        throw new AssertionError();
    }

    public static Integer getLoginUserNumber() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 로그인 한 경우
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                Integer userNumber = userDetails.getUserNumber();
                return userNumber;
            }
        }

        // 로그인 하지 않은 경우
        return null;
    }
}
