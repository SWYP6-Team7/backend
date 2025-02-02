package swyp.swyp6_team7.global.utils.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @RequireUserNumber 하면 API 요청 시 userNumber 가 자동으로 Resolving 되어 넘어감
// ex) public void api(@RequireUserNumber Integer userNumber)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireUserNumber {
}
