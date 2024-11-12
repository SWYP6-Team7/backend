package swyp.swyp6_team7.mock;

import org.springframework.security.test.context.support.WithSecurityContext;
import swyp.swyp6_team7.member.entity.UserRole;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomMockUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    String username() default "test@test.com";

    int userNumber() default 1;

    UserRole role() default UserRole.USER;

}
