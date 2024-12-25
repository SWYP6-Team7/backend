package swyp.swyp6_team7.community.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import swyp.swyp6_team7.category.domain.Category;
import swyp.swyp6_team7.category.repository.CategoryRepository;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.dto.request.CommunityCreateRequestDto;
import swyp.swyp6_team7.community.dto.response.CommunityDetailResponseDto;
import swyp.swyp6_team7.community.dto.response.CommunityListResponseDto;
import swyp.swyp6_team7.community.dto.response.CommunitySearchCondition;
import swyp.swyp6_team7.community.repository.CommunityCustomRepository;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.community.service.CommunityListService;
import swyp.swyp6_team7.community.service.CommunityService;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.service.ImageService;
import swyp.swyp6_team7.likes.dto.response.LikeReadResponseDto;
import swyp.swyp6_team7.likes.repository.LikeRepository;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.member.util.MemberAuthorizeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommunityService communityService;

    @MockBean
    private CommunityListService communityListService;

    @MockBean
    private CommunityRepository communityRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private LikeRepository likeRepository;

    @MockBean
    private ImageService imageService;

    @MockBean
    private CommunityCustomRepository communityCustomRepository;

    @Test
    @DisplayName("게시글 생성 - 성공적으로 게시글을 생성하고 상세 정보를 반환")
    void testCreatePost() throws Exception {
        Mockito.mockStatic(MemberAuthorizeUtil.class);
        Mockito.when(MemberAuthorizeUtil.getLoginUserNumber()).thenReturn(100);

        CommunityCreateRequestDto requestDto = CommunityCreateRequestDto.builder()
                .title("Test Title")
                .content("Test Content")
                .build();

        String formattedDate = CommunityDetailResponseDto.formatDate(LocalDateTime.now());
        CommunityDetailResponseDto responseDto = CommunityDetailResponseDto.builder()
                .postNumber(1)
                .userNumber(100)
                .postWriter("Test User")
                .categoryNumber(1)
                .categoryName("Test Category")
                .title("Test Title")
                .content("Test Content")
                .regDate(LocalDateTime.now())
                .commentCount(5)
                .viewCount(100)
                .likeCount(10)
                .liked(true)
                .profileImageUrl("http://example.com/profile.jpg")
                .build();

        Mockito.when(communityService.create(any(CommunityCreateRequestDto.class), anyInt()))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/community/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Test Title\", \"content\": \"Test Content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postNumber").value(1))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.regDate").exists())
                .andExpect(jsonPath("$.regDate").value(formattedDate));

        Mockito.clearAllCaches();

    }

    @Test
    @DisplayName("게시글 조회수 증가 및 상세 조회")
    void testIncreaseView() throws Exception {
        Mockito.mockStatic(MemberAuthorizeUtil.class);
        Mockito.when(MemberAuthorizeUtil.getLoginUserNumber()).thenReturn(100);

        // Mock 조회수 증가
        Mockito.doNothing().when(communityCustomRepository).incrementViewCount(Mockito.eq(1));

        CommunityDetailResponseDto mockResponse = CommunityDetailResponseDto.builder()
                .postNumber(1)
                .userNumber(100)
                .postWriter("Test User")
                .categoryNumber(1)
                .categoryName("Test Category")
                .title("Test Title")
                .content("Test Content")
                .regDate(LocalDateTime.now())
                .commentCount(10)
                .viewCount(50)
                .likeCount(20)
                .liked(true)
                .profileImageUrl("http://example.com/image.jpg")
                .build();

        // Mock 상세 조회
        Mockito.when(communityService.increaseView(Mockito.eq(1), Mockito.eq(100)))
                .thenReturn(mockResponse);

        // API 호출
        mockMvc.perform(get("/api/community/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postNumber").value(1));

        Mockito.clearAllCaches();
    }


//    @Test
//    @DisplayName("게시글 상세 조회 - 특정 게시글의 상세 정보를 반환")
//    void testGetDetail() throws Exception {
//        Mockito.mockStatic(MemberAuthorizeUtil.class);
//        Mockito.when(MemberAuthorizeUtil.getLoginUserNumber()).thenReturn(100);
//
//        String formattedDate = CommunityDetailResponseDto.formatDate(LocalDateTime.now());
//
//        CommunityDetailResponseDto mockResponse = CommunityDetailResponseDto.builder()
//                .postNumber(1)
//                .userNumber(100)
//                .postWriter("Test User")
//                .categoryNumber(1)
//                .categoryName("Test Category")
//                .title("Test Title")
//                .content("Test Content")
//                .regDate(LocalDateTime.now())
//                .commentCount(10)
//                .viewCount(50)
//                .likeCount(20)
//                .liked(true)
//                .profileImageUrl("http://example.com/image.jpg")
//                .build();
//
//        Mockito.when(communityService.getDetail(1, 123)).thenReturn(mockResponse);
//
//        mockMvc.perform(get("/api/community/posts/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.postNumber").value(1))
//                .andExpect(jsonPath("$.userNumber").value(100))
//                .andExpect(jsonPath("$.postWriter").value("Test User"))
//                .andExpect(jsonPath("$.title").value("Test Title"))
//                .andExpect(jsonPath("$.regDate").exists())
//                .andExpect(jsonPath("$.regDate").value(formattedDate));
//
//        Mockito.when(communityService.getDetail(Mockito.eq(1), Mockito.any())).thenReturn(mockResponse);
//
//        Mockito.clearAllCaches();
//    }

    @Test
    @DisplayName("게시글 상세 조회 - 특정 게시글의 상세 정보를 반환")
    void testGetDetail() throws Exception {
        Mockito.mockStatic(MemberAuthorizeUtil.class);
        Mockito.when(MemberAuthorizeUtil.getLoginUserNumber()).thenReturn(100);

        String formattedDate = CommunityDetailResponseDto.formatDate(LocalDateTime.now());

        Community mockCommunity = new Community(100, 1, "Test Title", "Test Content",  LocalDateTime.now(), 50);
        Users mockUser = new Users( 100,"test@example.com","testPW", "Test User", Gender.F,AgeGroup.TWENTY,null);
        Category mockCategory = new Category("전체");
        LikeReadResponseDto mockLikeStatus = new LikeReadResponseDto("community", 1, true, 20);
        ImageDetailResponseDto mockImageDetail = ImageDetailResponseDto.builder()
                .imageNumber(1L)
                .relatedType("profile")
                .relatedNumber(100)
                .key("profile_image_key")
                .url("http://example.com/image.jpg")
                .uploadDate(LocalDateTime.now())
                .build();

        // Mock repository and service calls
        Mockito.when(communityRepository.findByPostNumber(1)).thenReturn(Optional.of(mockCommunity));
        Mockito.when(userRepository.findByUserNumber(100)).thenReturn(Optional.of(mockUser));
        Mockito.when(categoryRepository.findByCategoryNumber(1)).thenReturn(Optional.of(mockCategory));
        Mockito.when(likeRepository.countByRelatedTypeAndRelatedNumber(Mockito.eq("community"), Mockito.eq(1)))
                .thenReturn(10L);
        Mockito.when(likeRepository.existsByRelatedTypeAndRelatedNumberAndUserNumber(Mockito.eq("community"), Mockito.eq(1), Mockito.eq(100)))
                .thenReturn(true);
        Mockito.when(imageService.getImageDetail(Mockito.eq("profile"), Mockito.eq(100), Mockito.eq(0)))
                .thenReturn(mockImageDetail);

        CommunityDetailResponseDto mockResponse = CommunityDetailResponseDto.builder()
                .postNumber(1)
                .userNumber(100)
                .postWriter("Test User")
                .categoryNumber(1)
                .categoryName("Test Category")
                .title("Test Title")
                .content("Test Content")
                .regDate(mockCommunity.getRegDate())
                .commentCount(10)
                .viewCount(50)
                .likeCount(20)
                .liked(true)
                .profileImageUrl("http://example.com/image.jpg")
                .build();

        Mockito.when(communityService.getDetail(1, 100)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/community/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postNumber").value(1))
                .andExpect(jsonPath("$.userNumber").value(100))
                .andExpect(jsonPath("$.postWriter").value("Test User"))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.regDate").exists())
                .andExpect(jsonPath("$.regDate").value(formattedDate));

        Mockito.verify(communityService, Mockito.times(1)).getDetail(Mockito.eq(1), Mockito.any());

        Mockito.clearAllCaches();
    }

}
