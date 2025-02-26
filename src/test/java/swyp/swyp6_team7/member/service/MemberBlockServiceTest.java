package swyp.swyp6_team7.member.service;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import swyp.swyp6_team7.member.entity.*;
import swyp.swyp6_team7.member.repository.*;

@SpringBootTest
public class MemberBlockServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserBlockRepository userBlockRepository;

    @Autowired
    private UserBlockReportRepository userBlockReportRepository;

    @Autowired
    private ReportReasonRepository reportReasonRepository;

    @Autowired
    private UserBlockExplanationRepository userBlockExplanationRepository;

    private Users createTestUser(String email) {
        Users user = new Users();
        user.setUserEmail(email);
        user.setUserName("Test User");
        user.setUserStatus(UserStatus.ABLE);
        user.setUserAgeGroup(AgeGroup.TEEN);
        user.setUserGender(Gender.F);
        user.setUserPw("testpw");
        return userRepository.save(user);
    }

    public void saveReportReason() {
        reportReasonRepository.save(new ReportReason(ReportCategory.DECEPTIVE_CONTENT, "허위정보"));
    }

    @Test
    @DisplayName("다른 유저 신고 정상처리")
    public void testUserReport() {
        Users user = createTestUser("test@abc.com");
    }
}
