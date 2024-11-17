package swyp.swyp6_team7.tag.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.domain.TravelTag;
import swyp.swyp6_team7.tag.repository.TagRepository;
import swyp.swyp6_team7.tag.repository.TravelTagRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TravelTagServiceTest {

    @Autowired
    private TravelTagService travelTagService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TravelTagRepository travelTagRepository;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private LocationRepository locationRepository;

    @AfterEach
    void tearDown() {
        travelTagRepository.deleteAllInBatch();
        travelRepository.deleteAllInBatch();
        tagRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
    }

    @DisplayName("update: 여행과 태그 이름 리스트가 주어지면, 변경된 TravelTag 리스트를 얻을 수 있다.")
    @Test
    void update() {
        // given
        Travel travel = travelRepository.save(createTravel(List.of()));
        List<String> newTags = List.of("쇼핑", "자연");

        // when
        List<TravelTag> travelTags = travelTagService.update(travel, newTags);

        // then
        assertThat(tagRepository.findAll()).hasSize(2);
        assertThat(travelTagRepository.findAll()).hasSize(0);

        assertThat(travelTags).hasSize(2)
                .map(travelTag -> travelTag.getTag())
                .extracting("name")
                .containsExactlyInAnyOrder("자연", "쇼핑");
    }

    @DisplayName("update: 여행과 태그 이름 리스트가 주어질 때, 더이상 필요없는 TravelTag는 삭제된다.")
    @Test
    void updateWithNoLongerNeedTravelTag() {
        // given
        Tag tag = tagRepository.save(Tag.of("먹방"));
        Travel travel = travelRepository.save(createTravel(Arrays.asList(tag)));

        List<String> newTags = List.of("쇼핑", "자연");

        // when
        List<TravelTag> travelTags = travelTagService.update(travel, newTags);

        // then
        assertThat(tagRepository.findAll()).hasSize(3);
        assertThat(travelTagRepository.findAll()).hasSize(0);

        assertThat(travelTags).hasSize(2)
                .map(travelTag -> travelTag.getTag())
                .extracting("name")
                .containsExactlyInAnyOrder("자연", "쇼핑");
    }


    private Travel createTravel(List<Tag> tags) {
        Location location = locationRepository.save(Location.builder()
                .locationName("여행지명")
                .locationType(LocationType.DOMESTIC)
                .build());

        return Travel.builder()
                .userNumber(1)
                .location(location)
                .viewCount(0)
                .genderType(GenderType.MIXED)
                .periodType(PeriodType.ONE_WEEK)
                .status(TravelStatus.IN_PROGRESS)
                .tags(tags)
                .build();
    }
}