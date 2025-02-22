package swyp.swyp6_team7.notification.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.companion.repository.CompanionRepository;
import swyp.swyp6_team7.config.SynchronousTaskExecutorConfig;
import swyp.swyp6_team7.notification.entity.CommunityCommentNotification;
import swyp.swyp6_team7.notification.entity.Notification;
import swyp.swyp6_team7.notification.repository.NotificationRepository;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@Import(SynchronousTaskExecutorConfig.class)
@SpringBootTest
class CommentNotificationServiceTest {

    @Autowired
    private CommentNotificationService commentNotificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private CommunityRepository communityRepository;

    @MockBean
    private TravelRepository travelRepository;

    @MockBean
    private CompanionRepository companionRepository;


    @AfterEach
    void tearDown() {
        notificationRepository.deleteAllInBatch();
    }

    @DisplayName("여행 댓글 알림을 생성할 수 있다.")
    @Test
    void createTravelCommentNotification() {
        // given
        Integer travelNumber = 10;
        Integer userNumber = 1;
        Travel targetTravel = createTravel(travelNumber, userNumber);
        given(travelRepository.findByNumber(anyInt()))
                .willReturn(Optional.of(targetTravel));

        // when
        commentNotificationService.createTravelCommentNotification(5, travelNumber);

        // then
        assertThat(notificationRepository.findAll()).hasSize(1)
                .extracting("receiverNumber", "title", "content", "isRead", "travelNumber")
                .contains(
                        tuple(1, "멤버 댓글 알림", "개설하신 [여행Title]에 멤버 댓글이 달렸어요. 확인해보세요.", false, 10)
                );
    }

    @DisplayName("여행 주최자는 자신이 생성한 댓글에 대해 알림을 받지 않는다.")
    @Test
    void createTravelCommentNotificationToHost() {
        // given
        Integer travelNumber = 10;
        Integer userNumber = 1;
        Travel targetTravel = createTravel(travelNumber, userNumber);
        given(travelRepository.findByNumber(anyInt()))
                .willReturn(Optional.of(targetTravel));

        // when
        commentNotificationService.createTravelCommentNotification(1, travelNumber);

        // then
        assertThat(notificationRepository.findAll()).isEmpty();
    }

    @DisplayName("여행 주최자와 참가자는 다른 사람의 댓글에 대한 알림을 받을 수 있다.")
    @Test
    void createTravelCommentNotificationToCompanions() {
        // given
        Integer travelNumber = 10;
        Integer userNumber = 1;
        Travel targetTravel = createTravel(travelNumber, userNumber);
        given(travelRepository.findByNumber(anyInt()))
                .willReturn(Optional.of(targetTravel));

        given(companionRepository.getUserNumbersByTravelNumber(anyInt()))
                .willReturn(List.of(3, 5));

        // when
        commentNotificationService.createTravelCommentNotification(3, travelNumber);

        List<Notification> notifications = notificationRepository.findAll();
        for (Notification notification : notifications) {
            System.out.println(notification);
        }

        // then
        assertThat(notificationRepository.findAll()).hasSize(2)
                .extracting("receiverNumber", "title", "content", "isRead", "travelNumber")
                .containsExactlyInAnyOrder(
                        tuple(1, "멤버 댓글 알림", "개설하신 [여행Title]에 멤버 댓글이 달렸어요. 확인해보세요.", false, 10),
                        tuple(5, "멤버 댓글 알림", "참가 신청하신 [여행Title]에 멤버 댓글이 달렸어요. 확인해보세요.", false, 10)
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
        commentNotificationService.createCommunityCommentNotification(5, postNumber);

        // then
        assertThat(notificationRepository.findAll()).hasSize(1)
                .extracting("receiverNumber", "title", "content", "isRead", "communityNumber", "notificationCount")
                .contains(
                        tuple(1, "커뮤니티", "[Test Title]에 댓글이 1개 달렸어요.", false, 10, 1)
                );
    }

    @DisplayName("커뮤니티 게시글에 대해 댓글 알림이 존재한다면, 기존 알림 데이터를 이용해 알림을 생성한다.")
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
        commentNotificationService.createCommunityCommentNotification(5, postNumber);

        // then
        assertThat(notificationRepository.findAll()).hasSize(1)
                .extracting("receiverNumber", "title", "content", "isRead", "communityNumber", "notificationCount")
                .contains(
                        tuple(1, "커뮤니티", "[Test Title]에 댓글이 2개 달렸어요.", false, 10, 2)
                );
    }

    @DisplayName("커뮤니티 게시물 작성자가 댓글을 추가한 경우, 알림이 생성되지 않는다.")
    @Test
    void communityCommentNotificationWhenPostOwner() {
        // given
        Integer postNumber = 10;
        Integer userNumber = 1;
        Community targetPost = createCommunityPost(postNumber, userNumber);
        given(communityRepository.findByPostNumber(anyInt()))
                .willReturn(Optional.of(targetPost));

        // when
        commentNotificationService.createCommunityCommentNotification(1, postNumber);

        // then
        assertThat(notificationRepository.findAll()).isEmpty();
    }

    private Travel createTravel(int travelNumber, int hostUserNumber) {
        return Travel.builder()
                .number(travelNumber)
                .userNumber(hostUserNumber)
                .title("여행Title")
                .maxPerson(2)
                .build();
    }

    private Community createCommunityPost(Integer postNumber, Integer userNumber) {
        return new Community(postNumber, userNumber, 1, "Test Title", "Test Content", LocalDateTime.now(), 0);
    }
}
