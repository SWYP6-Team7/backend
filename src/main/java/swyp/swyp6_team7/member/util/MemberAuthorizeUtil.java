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
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUserNumber();
    }
}
