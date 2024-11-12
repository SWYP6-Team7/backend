package swyp.swyp6_team7.mock;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import swyp.swyp6_team7.auth.details.CustomUserDetails;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;

import java.time.LocalDateTime;

public class WithCustomMockUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Users user = Users.builder()
                .userNumber(annotation.userNumber())
                .userEmail(annotation.username())
                .userPw("1234")
                .userName("유저명")
                .userGender(Gender.M)
                .userAgeGroup(AgeGroup.TWENTY)
                .role(annotation.role())
                .userRegDate(LocalDateTime.of(2024, 11, 1, 0, 0))
                .userStatus(UserStatus.ABLE)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        context.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        return context;
    }

}
