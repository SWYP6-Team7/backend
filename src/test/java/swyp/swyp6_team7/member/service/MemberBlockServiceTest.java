package swyp.swyp6_team7.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import swyp.swyp6_team7.member.dto.ReportReasonResponse;
import swyp.swyp6_team7.member.dto.UserBlockDetailResponse;
import swyp.swyp6_team7.member.entity.*;
import swyp.swyp6_team7.member.repository.ReportReasonRepository;
import swyp.swyp6_team7.member.repository.UserBlockReportRepository;
import swyp.swyp6_team7.member.repository.UserBlockRepository;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MemberBlockServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReportReasonRepository reportReasonRepository;
    @Autowired
    private UserBlockRepository userBlockRepository;
    @Autowired
    private UserBlockReportRepository userBlockReportRepository;
    @Autowired
    private MemberBlockService memberBlockService;

    private Users user1;
    private Users user2;
    private Users user3;
    private Users user4;

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

    @BeforeEach
    public void setup() {
        saveReportReason();
    }

    public void saveDummyBlockReport(
            int reporterUserNumber,
            int reportedUserNumber,
            int reportReasonId,
            String reportReasonExtra
    ) {
        userBlockReportRepository.save(
                new UserBlockReport(
                        reporterUserNumber,
                        reportedUserNumber,
                        reportReasonId,
                        reportReasonExtra
                )
        );
    }

    public void saveReportReason() {
        reportReasonRepository.save(new ReportReason(ReportCategory.OFFENSIVE_CONTENT, "다른 사람을 비하하거나 모욕했어요."));
        reportReasonRepository.save(new ReportReason(ReportCategory.OFFENSIVE_CONTENT, "공격적이거나 불쾌한 내용을 포함하고 있어요."));
        reportReasonRepository.save(new ReportReason(ReportCategory.SPAM, "똑같은 글이나 댓글을 반복해서 올렸어요."));
        reportReasonRepository.save(new ReportReason(ReportCategory.ILLEGAL_CONTENT, "특정 집단을 혐오하거나 차별하는 표현이 있어요."));
    }

    @Test
    @DisplayName("다른 유저 신고 정상처리 & 계정 정지 테스트")
    void testUserReport() {
        user1 = createTestUser("test@abc.com");
        user2 = createTestUser("reported@abc.com");
        user3 = createTestUser("reported2@abc.com");
        user4 = createTestUser("reported3@abc.com");

        List<ReportReasonResponse> reportReasons = memberBlockService.getAllReportReason();
        assertThat(reportReasons).isNotEmpty();

        ReportReasonResponse reportReason = reportReasons.getFirst();
        assertThat(reportReason.getReportDetailReasons()).isNotEmpty();

        int reportReasonId = reportReason.getReportDetailReasons().getFirst().getReportReasonId();
        String response = memberBlockService.report(
                user1.getUserNumber(),
                user2.getUserNumber(),
                reportReasonId,
                null
        );
        assertThat(response).isEqualTo("정상 처리되엇습니다.");

        List<UserBlockReport> userBlockReports = userBlockReportRepository.findAll();
        assertThat(userBlockReports).isNotEmpty();

        // 동일인의 재신고 시 처리 안됨
        String response2 = memberBlockService.report(
                user1.getUserNumber(),
                user2.getUserNumber(),
                reportReason.getReportDetailReasons().getFirst().getReportReasonId(),
                null
        );
        assertThat(response2).isNotEqualTo("정상 처리되엇습니다.");

        int user4Number = user4.getUserNumber();
        int user2Number = user2.getUserNumber();

        saveDummyBlockReport(user4Number + 1, user2Number, reportReasonId, null);
        saveDummyBlockReport(user4Number + 2, user2Number, reportReasonId, null);
        saveDummyBlockReport(user4Number + 3, user2Number, reportReasonId, null);

        // 타인의 신고 시 정상 처리됨
        String response3 = memberBlockService.report(
                user3.getUserNumber(),
                user2.getUserNumber(),
                reportReasonId,
                null
        );
        assertThat(response3).isEqualTo("정상 처리되었습니다.");

        List<UserBlock> blocks = userBlockRepository.findAllByUserNumberOrderByRegTs(user2Number);
        assertThat(blocks).isNotEmpty();

        UserBlockDetailResponse blockDetailResponse = memberBlockService.getBlockDetail(user2Number);
        // 5회 신고이기 때문에 계정 정지 상태
        assertThat(blockDetailResponse.isBlocked()).isEqualTo(false);
        assertThat(blockDetailResponse.getBlockPeriod()).isNull();

        saveDummyBlockReport(user4Number + 4, user2Number, reportReasonId, null);
        saveDummyBlockReport(user4Number + 5, user2Number, reportReasonId, null);
        saveDummyBlockReport(user4Number + 6, user2Number, reportReasonId, null);
        saveDummyBlockReport(user4Number + 7, user2Number, reportReasonId, null);

        // 10회째 신고 정상 처리
        String response4 = memberBlockService.report(
                user4.getUserNumber(),
                user2.getUserNumber(),
                reportReasonId,
                null
        );
        assertThat(response4).isEqualTo("정상 처리되었습니다.");

        List<UserBlock> blocks2 = userBlockRepository.findAllByUserNumberOrderByRegTs(user2Number);
        assertThat(blocks).isNotEmpty();

        UserBlockDetailResponse blockDetailResponse2 = memberBlockService.getBlockDetail(user2Number);
        // 5회 신고이기 때문에 계정 정지 상태
        assertThat(blockDetailResponse2.isBlocked()).isEqualTo(true);
        assertThat(blockDetailResponse2.getBlockPeriod()).isEqualTo(LocalDate.now().plusDays(90));
    }
}
