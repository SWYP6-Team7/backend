package swyp.swyp6_team7.auth.provider;


import java.util.Map;

public interface SocialLoginProvider {
    boolean supports(String provider);
    Map<String, String> getUserInfo(String code, String state);
}
