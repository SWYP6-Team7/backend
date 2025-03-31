package swyp.swyp6_team7.community.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import swyp.swyp6_team7.category.domain.Category;
import swyp.swyp6_team7.category.repository.CategoryRepository;
import swyp.swyp6_team7.comment.repository.CommentRepository;
import swyp.swyp6_team7.comment.service.CommentService;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.dto.request.CommunityUpdateRequestDto;
import swyp.swyp6_team7.community.dto.response.CommunityDetailResponseDto;
import swyp.swyp6_team7.community.repository.CommunityCustomRepository;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.service.ImageService;
import swyp.swyp6_team7.likes.repository.LikeRepository;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommunityServiceTest {
    @InjectMocks
    private CommunityService communityService;

    @Mock private CommunityRepository communityRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private LikeRepository likeRepository;
    @Mock private ImageService imageService;
    @Mock private CommentService commentService;
    @Mock private CommunityCustomRepository communityCustomRepository;

    private Users user;
    private Community community;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new Users(1, "test@example.com", "pw", "Tester", Gender.F, AgeGroup.TWENTY, null);
        community = new Community(1, 1, 1, "제목", "내용", LocalDateTime.now(), 5);
        category = new Category("잡담");
    }

    @Test
    @DisplayName("게시글 상세 조회 - 정상 케이스")
    void testCommunityDetail() {
        when(communityRepository.findByPostNumber(1)).thenReturn(Optional.of(community));
        when(userRepository.findByUserNumber(1)).thenReturn(Optional.of(user));
        when(categoryRepository.findByCategoryNumber(1)).thenReturn(Optional.of(category));
        when(commentRepository.countByRelatedTypeAndRelatedNumber("community", 1)).thenReturn(10L);
        when(likeRepository.existsByRelatedTypeAndRelatedNumberAndUserNumber("community", 1, 1)).thenReturn(true);
        when(likeRepository.countByRelatedTypeAndRelatedNumber("community", 1)).thenReturn(20L);
        when(imageService.getImageDetail("profile", 1, 0)).thenReturn(ImageDetailResponseDto.builder()
                .imageNumber(1L).relatedType("profile").relatedNumber(1).url("http://example.com/image.jpg").build());

        CommunityDetailResponseDto result = communityService.getCommunityDetail(1, 1);

        assertNotNull(result);
        assertEquals("제목", result.getTitle());
        assertEquals("Tester", result.getPostWriter());
        assertEquals(20, result.getLikeCount());
        assertTrue(result.isLiked());
        assertEquals(5, result.getViewCount());
    }

    @Test
    @DisplayName("게시글 상세 조회 시 게시글 없음")
    void testCommunityDetailPostNotFound() {
        when(communityRepository.findByPostNumber(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> communityService.getCommunityDetail(1, 1));

        assertEquals("게시글을 찾을 수 없습니다. postNumber=1", exception.getMessage());
    }

    @Test
    @DisplayName("게시글 상세 조회 시 작성자 정보 없음")
    void testCommunityDetailViewWriterNotFound() {
        when(communityRepository.findByPostNumber(1)).thenReturn(Optional.of(community));
        when(userRepository.findByUserNumber(1)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> communityService.getCommunityDetail(1, 1));

        assertEquals("작성자를 찾을 수 없습니다. userNumber=1", exception.getMessage());
    }

    @Test
    @DisplayName("커뮤니티 게시글 수정 성공")
    void testUpdateCommunityPost() {
        when(communityRepository.findByPostNumber(1)).thenReturn(Optional.of(community));
        when(userRepository.findByUserNumber(1)).thenReturn(Optional.of(user));
        when(categoryRepository.findByCategoryName("New Category")).thenReturn(Optional.of(category));
        when(categoryRepository.findByCategoryNumber(1)).thenReturn(Optional.of(category));
        when(commentRepository.countByRelatedTypeAndRelatedNumber("community", 1)).thenReturn(10L);
        when(likeRepository.existsByRelatedTypeAndRelatedNumberAndUserNumber("community", 1, 1)).thenReturn(true);
        when(likeRepository.countByRelatedTypeAndRelatedNumber("community", 1)).thenReturn(20L);
        when(imageService.getImageDetail("profile", 1, 0)).thenReturn(ImageDetailResponseDto.builder()
                .imageNumber(1L).relatedType("profile").relatedNumber(1).url("http://example.com/image.jpg").build());

        CommunityUpdateRequestDto updateRequest = new CommunityUpdateRequestDto("New Category", "New Title", "New Content");

        CommunityDetailResponseDto response = communityService.update(updateRequest, 1, 1);

        assertNotNull(response);
        assertEquals("New Title", response.getTitle());
        assertEquals("New Content", response.getContent());
        verify(communityRepository, times(2)).findByPostNumber(1);
    }

    @Test
    @DisplayName("커뮤니티 게시글 삭제 성공")
    void testDeleteCommunityPost() {
        when(communityRepository.findByPostNumber(1)).thenReturn(Optional.of(community));

        communityService.delete(1, 1);

        verify(communityRepository).delete(community);
    }


}
