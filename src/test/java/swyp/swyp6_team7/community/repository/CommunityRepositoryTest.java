package swyp.swyp6_team7.community.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.config.DataConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

@Import(DataConfig.class)
@DataJpaTest
public class CommunityRepositoryTest {
    @Autowired
    private CommunityRepository communityRepository;

    @BeforeEach
    void setup() {
        communityRepository.deleteAll();
    }

    @Test
    @DisplayName("게시글 저장 및 조회 테스트")
    void testSaveAndFindByPostNumber() {
        // Given: 게시글 저장
        Community community = new Community(1, 1, "Test Title", "Test Content", LocalDateTime.now(), 0);
        Community savedCommunity = communityRepository.saveAndFlush(community);

        // When: postNumber로 조회
        Optional<Community> retrievedCommunity = communityRepository.findByPostNumber(savedCommunity.getPostNumber());

        // Then: 조회 결과 검증
        assertThat(retrievedCommunity).isPresent();
        assertThat(retrievedCommunity.get().getPostNumber()).isEqualTo(savedCommunity.getPostNumber());
        assertThat(retrievedCommunity.get().getTitle()).isEqualTo("Test Title");
    }

    @Test
    @DisplayName("게시글 존재 여부 확인")
    void testExistsByPostNumber() {
        // Given: 게시글 저장
        Community community = new Community(1, 1, "Test Title", "Test Content", LocalDateTime.now(), 0);
        communityRepository.save(community);

        // When: 존재 여부 확인
        boolean exists = communityRepository.existsByPostNumber(1);

        // Then: 결과 검증
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회")
    void testFindByPostNumberNotFound() {
        // When: 존재하지 않는 게시글 조회
        Optional<Community> retrievedCommunity = communityRepository.findByPostNumber(999);

        // Then: 결과 검증
        assertThat(retrievedCommunity).isNotPresent();
    }

}
