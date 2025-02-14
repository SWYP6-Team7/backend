package swyp.swyp6_team7.notification.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import swyp.swyp6_team7.bookmark.repository.BookmarkRepository;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.companion.domain.Companion;
import swyp.swyp6_team7.config.SynchronousTaskExecutorConfig;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.enrollment.repository.EnrollmentRepository;
import swyp.swyp6_team7.notification.entity.CommunityCommentNotification;
import swyp.swyp6_team7.notification.repository.NotificationRepository;
import swyp.swyp6_team7.travel.domain.Travel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@Import(SynchronousTaskExecutorConfig.class)
@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private EnrollmentRepository enrollmentRepository;

    @MockBean
    private BookmarkRepository bookmarkRepository;

    @MockBean
    private CommunityRepository communityRepository;


    @AfterEach
    void tearDown() {
        notificationRepository.deleteAllInBatch();
    }

    @DisplayName("enrollNotification: 주어지는 사용자 번호와 여행 주최자에 대해 신청 생성 알림을 생성한다.")
    @Test
    void createEnrollNotification() {
        // given
        Travel targetTravel = createTravel(1);
        int enrollmentUserNumber = 2;

        // when
        notificationService.createEnrollNotification(targetTravel, enrollmentUserNumber);

        // then
        assertThat(notificationRepository.findAll()).hasSize(2)
                .extracting("receiverNumber", "title", "content", "isRead", "travelHost", "travelNumber", "travelTitle", "travelDueDate")
                .containsExactlyInAnyOrder(
                        tuple(1, "여행 신청 알림", "[여행Title]에 참가 신청자가 있어요. 알림을 눌러 확인해보세요.", false, true, 10, "여행Title", LocalDate.of(2024, 11, 16)),
                        tuple(2, "참가 신청 알림", "[여행Title]에 참가 신청이 완료되었어요. 주최자가 참가를 확정하면 알려드릴게요.", false, false, 10, "여행Title", LocalDate.of(2024, 11, 16))
                );
    }

    @DisplayName("acceptNotification: 주어지는 사용자 번호에 대해 신청 수락 알림을 생성한다.")
    @Test
    void createAcceptNotification() {
        // given
        Travel targetTravel = createTravel(1);
        int enrollmentUserNumber = 2;

        // when
        notificationService.createAcceptNotification(targetTravel, enrollmentUserNumber);

        // then
        assertThat(notificationRepository.findAll()).hasSize(1)
                .extracting("receiverNumber", "title", "content", "isRead", "travelHost", "travelNumber", "travelTitle", "travelDueDate")
                .contains(tuple(2, "참가 확정 알림", "[여행Title]에 참가가 확정되었어요. 멤버 댓글을 통해 인사를 나눠보세요.", false, false, 10, "여행Title", LocalDate.of(2024, 11, 16)));
    }

    @DisplayName("rejectNotification: 주어지는 사용자 번호에 대해 신청 거절 알림을 생성한다.")
    @Test
    void createRejectNotification() {
        // given
        Travel targetTravel = createTravel(1);
        int enrollmentUserNumber = 2;

        // when
        notificationService.createRejectNotification(targetTravel, enrollmentUserNumber);

        // then
        assertThat(notificationRepository.findAll()).hasSize(1)
                .extracting("receiverNumber", "title", "content", "isRead", "travelHost", "travelNumber", "travelTitle", "travelDueDate")
                .contains(tuple(2, "참가 거절 알림", "[여행Title]에 참가가 아쉽게도 거절되었어요. 다른 여행을 찾아볼까요?", false, false, 10, "여행Title", LocalDate.of(2024, 11, 16)));
    }

    @DisplayName("companionClosedNotification: 주최자, 참여자, pending 상태의 신청자, 즐겨찾기 사용자에 대해 인원 마감 알림을 생성한다.")
    @Test
    void createCompanionClosedNotification() {
        // given
        Travel targetTravel = createTravel(1);
        createCompanion(targetTravel, 2);

        given(enrollmentRepository.findUserNumbersByTravelNumberAndStatus(anyInt(), any(EnrollmentStatus.class)))
                .willReturn(Arrays.asList(3));
        given(bookmarkRepository.findUserNumberByTravelNumber(anyInt()))
                .willReturn(Arrays.asList(1, 2, 3, 4));

        // when
        notificationService.createCompanionClosedNotification(targetTravel);

        // then
        assertThat(notificationRepository.findAll()).hasSize(4)
                .extracting("receiverNumber", "title", "content", "isRead", "travelHost", "travelNumber", "travelTitle", "travelDueDate")
                .containsExactlyInAnyOrder(
                        tuple(1, "모집 마감 알림", "[여행Title]의 인원이 가득 차 모집이 마감되었어요.", false, true, 10, "여행Title", LocalDate.of(2024, 11, 16)),
                        tuple(2, "모집 마감 알림", "참가하신 [여행Title]의 모집이 마감되었어요.", false, false, 10, "여행Title", LocalDate.of(2024, 11, 16)),
                        tuple(3, "모집 마감 알림", "참가 신청하신 [여행Title]의 모집이 마감되었어요.", false, false, 10, "여행Title", LocalDate.of(2024, 11, 16)),
                        tuple(4, "모집 마감 알림", "즐겨찾기하신 [여행Title]의 모집이 마감되었어요.", false, false, 10, "여행Title", LocalDate.of(2024, 11, 16))
                );
    }

    @DisplayName("커뮤니티 댓글 알림을 생성할 수 있다.")
    @Test
    void createCommunityCommentNotification() {
        // given
        Integer postNumber = 10;
        Integer userNumber = 1;
        Community targetPost = createCommunityPost(postNumber, userNumber);
        given(communityRepository.findByPostNumber(anyInt()))
                .willReturn(Optional.of(targetPost));

        // when
        notificationService.createCommentNotifications(5, "community", postNumber);

        // then
        assertThat(notificationRepository.findAll()).hasSize(1)
                .extracting("receiverNumber", "title", "content", "isRead", "communityNumber", "notificationCount")
                .contains(
                        tuple(1,"커뮤니티", "[Test Title]에 댓글이 1개 달렸어요.", false, 10, 1)
                );
    }

    @DisplayName("특정 사용자의 가장 최신 알림이 동일한 커뮤니티 게시글에 대한 댓글 알림이고 읽지 않은 경우, 기존 알림 데이터를 이용해 알림을 생성한다.")
    @Test
    void communityCommentNotificationWhenReuse() {
        // given
        Integer postNumber = 10;
        Integer userNumber = 1;
        Community targetPost = createCommunityPost(postNumber, userNumber);

        CommunityCommentNotification newNotification = CommunityCommentNotification.builder()
                .receiverNumber(targetPost.getUserNumber())
                .title("커뮤니티")
                .content(String.format("[%s]에 댓글이 %d개 달렸어요.", targetPost.getTitle(), 1))
                .isRead(false)
                .communityNumber(targetPost.getPostNumber())
                .notificationCount(1)
                .build();
        notificationRepository.save(newNotification);

        given(communityRepository.findByPostNumber(anyInt()))
                .willReturn(Optional.of(targetPost));

        // when
        notificationService.createCommentNotifications(5, "community", postNumber);

        // then
        assertThat(notificationRepository.findAll()).hasSize(1)
                .extracting("receiverNumber", "title", "content", "isRead", "communityNumber", "notificationCount")
                .contains(
                        tuple(1,"커뮤니티", "[Test Title]에 댓글이 2개 달렸어요.", false, 10, 2)
                );
    }

    private Companion createCompanion(Travel travel, int userNumber) {
        return Companion.builder()
                .travel(travel)
                .userNumber(userNumber)
                .build();
    }

    private Travel createTravel(int hostUserNumber) {
        return Travel.builder()
                .number(10)
                .userNumber(hostUserNumber)
                .title("여행Title")
                .maxPerson(2)
                .dueDate(LocalDate.of(2024, 11, 16))
                .build();
    }

    private Community createCommunityPost(Integer postNumber, Integer userNumber) {
        return new Community(postNumber, userNumber, 1, "Test Title", "Test Content", LocalDateTime.now(), 0);
    }
}
