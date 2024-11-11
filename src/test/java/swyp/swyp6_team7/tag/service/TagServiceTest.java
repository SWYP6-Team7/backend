package swyp.swyp6_team7.tag.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.repository.TagRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TagServiceTest {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRepository tagRepository;

    @AfterEach
    void tearDown() {
        tagRepository.deleteAllInBatch();
    }


    @DisplayName("findByName: 태그 이름이 주어질 때, 태그를 가져올 수 있다.")
    @Test
    void findByName() {
        // given
        String tagName = "태그 이름";
        tagRepository.save(Tag.of(tagName));

        // when
        Tag tag = tagService.findByName(tagName);

        // then
        assertThat(tagRepository.findAll()).hasSize(1)
                .extracting("name")
                .contains("태그 이름");
        assertThat(tag.getName()).isEqualTo("태그 이름");
    }

    @DisplayName("findByName: 존재하지 않는 태그인 경우 새로운 Tag를 생성한다.")
    @Test
    void findByNameWithCreateNewTag() {
        // given
        String newTagName = "새로운 태그";

        // when
        Tag tag = tagService.findByName(newTagName);

        // then
        assertThat(tagRepository.findAll()).hasSize(1)
                .extracting("name")
                .contains("새로운 태그");
        assertThat(tag.getName()).isEqualTo("새로운 태그");
    }

}