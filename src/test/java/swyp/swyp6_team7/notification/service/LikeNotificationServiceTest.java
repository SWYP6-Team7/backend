package swyp.swyp6_team7.notification.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.config.SynchronousTaskExecutorConfig;
import swyp.swyp6_team7.notification.entity.CommunityPostLikeNotification;
import swyp.swyp6_team7.notification.repository.NotificationRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Import(SynchronousTaskExecutorConfig.class)
@SpringBootTest
@Disabled
class LikeNotificationServiceTest {

    @Autowired
    private LikeNotificationService likeNotificationService;

    @Autowired
    private NotificationRepository notificationRepository;


    @AfterEach
    void tearDown() {
        notificationRepository.deleteAllInBatch();
    }

    @DisplayName("커뮤니티 게시글 좋아요에 대해 알림을 생성할 수 있다.")
    @Test
    void createCommunityPostLikeNotification() {
        // given
        Integer postNumber = 10;
        Integer userNumber = 5;
        Community targetPost = createCommunityPost(postNumber, userNumber);

        // when
        likeNotificationService.createCommunityPostLikeNotification(targetPost);

        // then
        assertThat(notificationRepository.findAll()).hasSize(1)
                .extracting("receiverNumber", "title", "content", "isRead", "communityNumber", "notificationCount")
                .contains(
                        tuple(5, "커뮤니티", "[Test Title]에 좋아요가 1개 달렸어요.", false, 10, 1)
                );
    }

    @DisplayName("커뮤니티 게시글에 대해 좋아요 알림이 존재한다면, 기존 알림 데이터를 이용해 알림을 생성한다.")
    @Test
    void likeCommunityPostLikeNotificationWhenReuse() {
        // given
        Integer postNumber = 10;
        Integer userNumber = 5;
        Community targetPost = createCommunityPost(postNumber, userNumber);

        CommunityPostLikeNotification newNotification = CommunityPostLikeNotification.builder()
                .receiverNumber(targetPost.getUserNumber())
                .title("커뮤니티")
                .content(String.format("[%s]에 좋아요가 %d개 달렸어요.", targetPost.getTitle(), 1))
                .isRead(false)
                .communityNumber(targetPost.getPostNumber())
                .notificationCount(1)
                .build();
        notificationRepository.save(newNotification);

        // when
        likeNotificationService.createCommunityPostLikeNotification(targetPost);

        // then
        assertThat(notificationRepository.findAll()).hasSize(1)
                .extracting("receiverNumber", "title", "content", "isRead", "communityNumber", "notificationCount")
                .contains(
                        tuple(5, "커뮤니티", "[Test Title]에 좋아요가 2개 달렸어요.", false, 10, 2)
                );
    }

    private Community createCommunityPost(Integer postNumber, Integer userNumber) {
        return new Community(postNumber, userNumber, 1, "Test Title", "Test Content", LocalDateTime.now(), 0);
    }
}
