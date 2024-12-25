package swyp.swyp6_team7.community.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import swyp.swyp6_team7.category.domain.Category;
import swyp.swyp6_team7.category.repository.CategoryRepository;
import swyp.swyp6_team7.comment.repository.CommentRepository;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.dto.request.CommunityUpdateRequestDto;
import swyp.swyp6_team7.community.dto.response.CommunityDetailResponseDto;
import swyp.swyp6_team7.community.repository.CommunityCustomRepository;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.service.ImageService;
import swyp.swyp6_team7.likes.dto.response.LikeReadResponseDto;
import swyp.swyp6_team7.likes.repository.LikeRepository;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CommunityServiceTest {
    @Autowired
    private CommunityService communityService;

    @MockBean
    private CommunityRepository communityRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private CommentRepository commentRepository;

    @MockBean
    private LikeRepository likeRepository;

    @MockBean
    private ImageService imageService;

    @MockBean
    private CommunityCustomRepository communityCustomRepository;

    @Test
    @DisplayName("게시글 조회수 증가 및 상세 조회 - 정상 케이스")
    void testIncreaseView() {
        // Given
        Community mockCommunity = new Community(1,100, 1, "Test Title", "Test Content",  LocalDateTime.now(), 50);
        Category mockCategory = new Category("전체");

        Mockito.doAnswer(invocation -> {
            mockCommunity.setViewCount(mockCommunity.getViewCount() + 1); // viewCount 증가
            return null;
        }).when(communityCustomRepository).incrementViewCount(1);

        Mockito.when(communityRepository.findByPostNumber(1))
                .thenReturn(Optional.of(mockCommunity));
        Mockito.when(userRepository.findByUserNumber(100))
                .thenReturn(Optional.of(new Users(100, "test@example.com", "testPW", "Test User", Gender.F, AgeGroup.TWENTY, null)));
        Mockito.when(categoryRepository.findByCategoryNumber(1))
                .thenReturn(Optional.of(mockCategory));
        Mockito.when(commentRepository.countByRelatedTypeAndRelatedNumber("community", 1))
                .thenReturn(10L);
        Mockito.when(likeRepository.existsByRelatedTypeAndRelatedNumberAndUserNumber(Mockito.eq("community"), Mockito.eq(1), Mockito.eq(100)))
                .thenReturn(true);
        Mockito.when(likeRepository.countByRelatedTypeAndRelatedNumber("community", 1))
                .thenReturn(20L);
        Mockito.when(imageService.getImageDetail("profile", 100, 0))
                .thenReturn(ImageDetailResponseDto.builder()
                        .imageNumber(1L)
                        .relatedType("profile")
                        .relatedNumber(100)
                        .url("http://example.com/image.jpg")
                        .build());


        // When
        CommunityDetailResponseDto result = communityService.increaseView(1, 100);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getPostNumber());
        assertEquals(100, result.getUserNumber());
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test User", result.getPostWriter());
        assertEquals(20, result.getLikeCount());
        assertEquals(51, result.getViewCount());
        assertTrue(result.isLiked());
    }

    @Test
    @DisplayName("게시글 상세 조회 시 게시글 없음")
    void testIncreaseViewPostNotFound() {
        // Given: Mock 데이터 설정
        Mockito.when(communityRepository.findByPostNumber(1))
                .thenReturn(Optional.empty());

        // When & Then: 예외 발생 검증
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            communityService.increaseView(1, 100);
        });

        assertEquals("게시글을 찾을 수 없습니다. postNumber=1", exception.getMessage());
    }

    @Test
    @DisplayName("게시글 상세 조회 시 작성자 정보 없음")
    void testIncreaseViewWriterNotFound() {
        // Given: Mock 데이터 설정
        Community mockCommunity = new Community(100, 1, "Test Title", "Test Content",  LocalDateTime.now(), 50);

        Mockito.when(communityRepository.findByPostNumber(1))
                .thenReturn(Optional.of(mockCommunity));
        Mockito.when(userRepository.findByUserNumber(100))
                .thenReturn(Optional.empty());

        // When & Then: 예외 발생 검증
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            communityService.increaseView(1, 100);
        });

        assertEquals("작성자를 찾을 수 없습니다. userNumber=100", exception.getMessage());
    }

    @Test
    @DisplayName("커뮤니티 게시글 수정 성공")
    void testUpdateCommunityPost() {
        Community mockCommunity = new Community(1,1, 1, "Old Title", "Old Content", LocalDateTime.now(), 0);
        Mockito.when(communityRepository.findByPostNumber(1))
                .thenReturn(Optional.of(mockCommunity));
        Mockito.when(userRepository.findByUserNumber(1))
                .thenReturn(Optional.of(new Users(1, "test@example.com", "testPW", "Test User", Gender.F, AgeGroup.TWENTY, null)));
        Mockito.when(commentRepository.countByRelatedTypeAndRelatedNumber("community", 1))
                .thenReturn(10L);
        Mockito.when(likeRepository.existsByRelatedTypeAndRelatedNumberAndUserNumber(Mockito.eq("community"), Mockito.eq(1), Mockito.eq(1)))
                .thenReturn(true);
        Mockito.when(likeRepository.countByRelatedTypeAndRelatedNumber("community", 1))
                .thenReturn(20L);
        Mockito.when(imageService.getImageDetail("profile", 1, 0))
                .thenReturn(ImageDetailResponseDto.builder()
                        .imageNumber(1L)
                        .relatedType("profile")
                        .relatedNumber(1)
                        .url("http://example.com/image.jpg")
                        .build());

        CommunityUpdateRequestDto updateRequest = new CommunityUpdateRequestDto("New Category","New Title", "New Content" );
        Mockito.when(categoryRepository.findByCategoryName("New Category"))
                .thenReturn(Optional.of(new Category( "New Category")));

        CommunityDetailResponseDto response = communityService.update(updateRequest, 1, 1);

        assertNotNull(response);
        assertEquals("New Title", response.getTitle());
        Mockito.verify(communityRepository,Mockito.times(2)).findByPostNumber(1);
    }

    @Test
    @DisplayName("커뮤니티 게시글 삭제 성공")
    void testDeleteCommunityPost() {
        Community mockCommunity = new Community(1, 1, "Test Title", "Test Content", LocalDateTime.now(), 0);
        Mockito.when(communityRepository.findByPostNumber(1))
                .thenReturn(Optional.of(mockCommunity));

        communityService.delete(1, 1);

        Mockito.verify(communityRepository).delete(mockCommunity);
    }


}
