package swyp.swyp6_team7.comment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.comment.domain.Comment;
import swyp.swyp6_team7.comment.dto.request.CommentCreateRequestDto;
import swyp.swyp6_team7.comment.dto.response.CommentDetailResponseDto;
import swyp.swyp6_team7.comment.dto.response.CommentListReponseDto;
import swyp.swyp6_team7.comment.repository.CommentRepository;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.likes.repository.LikeRepository;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.notification.service.CommentNotificationService;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private TravelRepository travelRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private CommentNotificationService notificationService;

    @Test
    @DisplayName("댓글 생성 성공")
    void createComment_Success() {
        // Given
        CommentCreateRequestDto request = new CommentCreateRequestDto("Test Comment",0);
        int userNumber = 1;
        String relatedType = "community";
        int relatedNumber = 100;

        Comment savedComment = new Comment(1,  "Test Comment", 0
                , LocalDateTime.now(),relatedType, relatedNumber);

        given(communityRepository.existsByPostNumber(relatedNumber)).willReturn(true);
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // When
        Comment result = commentService.create(request, userNumber, relatedType, relatedNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Test Comment");
        verify(notificationService).createCommunityCommentNotification(userNumber, relatedNumber);
    }

    @Test
    @DisplayName("댓글 생성 실패 - 부모 댓글이 존재하지 않음")
    void createComment_Fail_NoParentComment() {
        // Given
        CommentCreateRequestDto request = new CommentCreateRequestDto( "Test Reply",999);
        int userNumber = 1;
        String relatedType = "community";
        int relatedNumber = 100;

        given(communityRepository.existsByPostNumber(relatedNumber)).willReturn(true);
        given(commentRepository.existsByCommentNumber(request.getParentNumber())).willReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> commentService.create(request, userNumber, relatedType, relatedNumber));

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 상세 조회 성공")
    void getCommentByNumber_Success() {
        // Given
        int commentNumber = 1;
        Comment comment = new Comment(1, "Test Comment", 1,
                LocalDateTime.now(),"community",100);
        given(commentRepository.findByCommentNumber(commentNumber)).willReturn(Optional.of(comment));
        given(likeRepository.countByRelatedTypeAndRelatedNumber("comment", commentNumber)).willReturn(10L);

        // When
        CommentDetailResponseDto result = commentService.getCommentByNumber(commentNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLikes()).isEqualTo(10L);
        assertThat(result.getContent()).isEqualTo("Test Comment");
    }

    @Test
    @DisplayName("댓글 상세 조회 실패 - 댓글이 존재하지 않음")
    void getCommentByNumber_Fail_NotFound() {
        // Given
        int commentNumber = 1;
        given(commentRepository.findByCommentNumber(commentNumber)).willReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> commentService.getCommentByNumber(commentNumber));
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getList_Success() {
        // Given
        String relatedType = "community";
        int relatedNumber = 100;
        Integer userNumber = 1;

        Comment comment = new Comment(1, "Test Comment", 0,
                 LocalDateTime.now(),relatedType,relatedNumber);

        given(communityRepository.existsByPostNumber(relatedNumber)).willReturn(true);
        given(commentRepository.findByRelatedTypeAndRelatedNumber(relatedType, relatedNumber))
                .willReturn(List.of(comment));

        // When
        List<CommentListReponseDto> result = commentService.getList(relatedType, relatedNumber, userNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Test Comment");
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_Success() {
        // Given
        int commentNumber = 1;
        int userNumber = 1;
        Comment comment = new Comment(1,1, "Test Comment", 1,
                LocalDateTime.now(),"community",100);

        given(commentRepository.findByCommentNumber(commentNumber)).willReturn(Optional.of(comment));
        given(commentRepository.findByRelatedTypeAndRelatedNumberAndParentNumber(
                comment.getRelatedType(), comment.getRelatedNumber(), comment.getCommentNumber()))
                .willReturn(Collections.emptyList());
        given(communityRepository.findByPostNumber(comment.getRelatedNumber()))
                .willReturn(Optional.of(new Community(100,1, 1, "Test Title", "Test Content", LocalDateTime.now(), 0)));


        // When
        commentService.delete(commentNumber, userNumber);

        // Then
        verify(commentRepository).deleteByCommentNumber(commentNumber);
        verify(likeRepository).deleteByRelatedTypeAndRelatedNumber("comment", commentNumber);
    }
}
