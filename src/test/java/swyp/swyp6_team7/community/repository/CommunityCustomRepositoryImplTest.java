package swyp.swyp6_team7.community.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import swyp.swyp6_team7.category.domain.Category;
import swyp.swyp6_team7.category.repository.CategoryRepository;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.dto.response.CommunitySearchCondition;
import swyp.swyp6_team7.community.dto.response.CommunitySearchDto;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.community.util.CommunitySearchSortingType;
import swyp.swyp6_team7.likes.domain.Like;
import swyp.swyp6_team7.likes.repository.LikeRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CommunityCustomRepositoryImplTest {

    @Autowired
    private CommunityCustomRepositoryImpl communityCustomRepository;
    @Autowired
    private CommunityRepository communityRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private LikeRepository likeRepository;

    @BeforeEach
    void setUp(){
        categoryRepository.save(new Category("Test Category1"));
        categoryRepository.save(new Category("Test Category2"));
    }

    @Test
    @DisplayName("게시글 조회수 증가 테스트")
    void testIncrementViewCount() {
        // Given
        Category category = categoryRepository.findAll().get(0);
        Community community = communityRepository.save(new Community(1,category.getCategoryNumber(),"First Post","First Content",LocalDateTime.now(),0));

        assertThat(community.getViewCount()).isEqualTo(0);

        // When: 조회수 증가
        communityCustomRepository.incrementViewCount(community.getPostNumber());

        // Then: 조회수 증가 확인
        Community updatedCommunity = communityRepository.findByPostNumber((community.getPostNumber())).orElseThrow();
        assertThat(updatedCommunity.getViewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시글 키워드 검색 테스트")
    void testSearchWithKeyword() {
        // Given
        Category category = categoryRepository.findAll().get(0);
        Community community = communityRepository.save(new Community(1,category.getCategoryNumber(), "First Post","First Content",LocalDateTime.now(),0));
        CommunitySearchCondition condition = new CommunitySearchCondition(
                PageRequest.of(0, 5),
                "First", // 키워드
                null,    // 카테고리 번호 없음
                "REG_DATE_DESC"
        );

        // When: 키워드 검색 실행
        List<CommunitySearchDto> results = communityCustomRepository.search(condition);

        // Then: 결과 검증
        assertThat(results).isNotEmpty();
        CommunitySearchDto dto = results.get(0);
        assertThat(dto.getCommunity().getTitle()).contains("First");
        assertThat(dto.getCategoryName()).isEqualTo("Test Category1");
        assertThat(dto.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("카테고리 필터링 테스트")
    void testSearchWithCategory() {
        // Given
        List<Category> categories = categoryRepository.findAll();
        Category category1 = categories.get(0);
        Category category2 = categories.get(1);
        Community community1 = communityRepository.save(new Community(1, 1, "First Post", "First Content", LocalDateTime.now(), 0));
        Community community2 = communityRepository.save(new Community(2, 2, "Second Post", "Second Content", LocalDateTime.now(), 0));

        CommunitySearchCondition condition = new CommunitySearchCondition(
                PageRequest.of(0, 5),
                null, // 키워드 없음
                2,    // 카테고리 번호
                "REG_DATE_DESC"
        );

        // When: 카테고리 필터 검색 실행
        List<CommunitySearchDto> results = communityCustomRepository.search(condition);

        // Then: 결과 검증
        assertThat(results).hasSize(1);
        results.forEach(dto -> assertThat(dto.getCategoryName()).isEqualTo("Test Category2"));
    }

    @Test
    @DisplayName("게시글 정렬 테스트 (좋아요 순)")
    void testSearchWithSorting() {
        // Given
        Category category = categoryRepository.findAll().get(0);
        Community community1 = communityRepository.save(new Community(1, category.getCategoryNumber(), "First Post", "First Content", LocalDateTime.now(), 0));
        Community community2 = communityRepository.save(new Community(2, category.getCategoryNumber(), "Second Post", "Second Content", LocalDateTime.now(), 0));
        likeRepository.saveAll(Arrays.asList(
                new Like("community", community1.getPostNumber(), 1),
                new Like("community", community1.getPostNumber(), 2)
        ));

        CommunitySearchCondition condition = new CommunitySearchCondition(
                PageRequest.of(0, 5),
                null, // 키워드 없음
                null, // 카테고리 번호 없음
                "LIKE_COUNT_DESC"
        );

        // When: 정렬 조건 검색 실행
        List<CommunitySearchDto> results = communityCustomRepository.search(condition);


        // Then: 결과 검증 (좋아요 순 정렬)
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getLikeCount()).isGreaterThanOrEqualTo(results.get(1).getLikeCount());
    }

    @Test
    @DisplayName("사용자별 게시글 조회 테스트")
    void testGetMyList() {
        // given
        Category category = categoryRepository.findAll().get(0);
        Community community = communityRepository.save(new Community(123, category.getCategoryNumber(), "First Post", "First Content", LocalDateTime.now(), 0));

        CommunitySearchCondition condition = new CommunitySearchCondition(
                PageRequest.of(0, 5),
                null, // 키워드 없음
                null, // 카테고리 번호 없음
                "REG_DATE_DESC"
        );

        // when
        List<CommunitySearchDto> results = communityCustomRepository.getMyList(
                CommunitySearchSortingType.REG_DATE_DESC,
                community.getUserNumber()
        );

        // then
        assertThat(results).hasSize(1);
        CommunitySearchDto dto = results.get(0);
        assertThat(dto.getCommunity().getUserNumber()).isEqualTo(community.getUserNumber());
        assertThat(dto.getCategoryName()).isEqualTo("Test Category1");
    }
}
