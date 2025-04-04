package swyp.swyp6_team7.member.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.member.dto.UserReportRequest;
import swyp.swyp6_team7.member.entity.ReportCategory;
import swyp.swyp6_team7.member.entity.ReportReason;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.ReportReasonRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MemberBlockControllerTest extends IntegrationTest {

    private static int reportReasonId;
    private static int reporterUserId;
    private static int reporteeUserId;
    private static String reporterJwtToken;
    @Autowired
    private ReportReasonRepository reportReasonRepository;

    @BeforeAll
    void setup() {
        Users reporter = createUser("reporter", "password");
        Users reportee = createUser("reportee", "password");
        reporterUserId = reporter.getUserNumber();
        reporteeUserId = reportee.getUserNumber();
        reporterJwtToken = login("reporter@test.com", "password").getAccessToken();

        ReportReason reason = reportReasonRepository.save(new ReportReason(ReportCategory.SPAM, "스팸 게시글"));
        reportReasonId = reason.getId();
        reportReasonRepository.save(new ReportReason(ReportCategory.ILLEGAL_CONTENT, "저작권 침해"));
    }

    @Test
    @DisplayName("상대방 신고 정상처리")
    @Transactional
    public void testCheckEmailDuplicate() throws Exception {
        UserReportRequest request = new UserReportRequest(reporteeUserId, reportReasonId, null);

        mockMvc.perform(post("/api/member/block")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + reporterJwtToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("정상 처리되었습니다."));

        mockMvc.perform(post("/api/member/block")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + reporterJwtToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("이미 신고한 이력이 있습니다."));
    }
}
