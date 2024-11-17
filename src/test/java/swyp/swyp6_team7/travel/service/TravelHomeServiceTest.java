package swyp.swyp6_team7.travel.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.repository.TagRepository;
import swyp.swyp6_team7.tag.repository.TravelTagRepository;
import swyp.swyp6_team7.tag.repository.UserTagPreferenceRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.TravelRecommendDto;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static swyp.swyp6_team7.travel.domain.TravelStatus.IN_PROGRESS;


@SpringBootTest
class TravelHomeServiceTest {

    @Autowired
    private TravelHomeService travelHomeService;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private TravelTagRepository travelTagRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private LocationRepository locationRepository;

    @MockBean
    private UserTagPreferenceRepository userTagPreferenceRepository;

    @AfterEach
    void tearDown() {
        travelTagRepository.deleteAllInBatch();
        travelRepository.deleteAllInBatch();
        tagRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
    }


    @DisplayName("사용자의 선호 태그와 공통되는 여행 태그 개수 preferredNumber가 큰 순서대로 여행 목록을 가져온다.")
    @Test
    void getRecommendTravelsByUser() {
        // given
        Tag tag1 = Tag.of("쇼핑");
        Tag tag2 = Tag.of("자연");
        Tag tag3 = Tag.of("먹방");
        Tag tag4 = Tag.of("즉흥");
        tagRepository.saveAll(List.of(tag1, tag2, tag3, tag4));

        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC));
        LocalDate dueDate = LocalDate.of(2024, 11, 7);
        Travel travel1 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                dueDate, PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1));
        Travel travel2 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                dueDate, PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag2));
        Travel travel3 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                dueDate.plusDays(1), PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag4));
        Travel travel4 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                dueDate, PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag2, tag3));
        travelRepository.saveAll(List.of(travel1, travel2, travel3, travel4));

        LocalDate requestDate = LocalDate.of(2024, 11, 6);

        given(userTagPreferenceRepository.findPreferenceTagsByUserNumber(any(Integer.class)))
                .willReturn(Arrays.asList("쇼핑", "자연", "먹방"));

        // when
        Page<TravelRecommendDto> result = travelHomeService.getRecommendTravelsByUser(PageRequest.of(0, 5), 1, requestDate);

        // then
        assertThat(result.getContent()).hasSize(4)
                .extracting("travelNumber", "tags", "preferredNumber", "registerDue")
                .containsExactly(
                        tuple(travel4.getNumber(), Arrays.asList("쇼핑", "자연", "먹방"), 3, dueDate),
                        tuple(travel2.getNumber(), Arrays.asList("쇼핑", "자연"), 2, dueDate),
                        tuple(travel1.getNumber(), Arrays.asList("쇼핑"), 1, dueDate),
                        tuple(travel3.getNumber(), Arrays.asList("쇼핑", "즉흥"), 1, dueDate.plusDays(1))
                );
    }

    @DisplayName("사용자의 선호 태그와 공통되는 여행 태그 개수 preferredNumber가 동일한 경우 registerDue가 빠른 순서대로 여행을 가져온다.")
    @Test
    void getRecommendTravelsByUserWhenSamePreferredNumber() {
        Tag tag1 = Tag.of("쇼핑");
        Tag tag2 = Tag.of("자연");
        tagRepository.saveAll(List.of(tag1, tag2));

        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC));
        LocalDate dueDate = LocalDate.of(2024, 11, 7);
        Travel travel1 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                dueDate.plusDays(1), PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag2));
        Travel travel2 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                dueDate, PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag2));
        travelRepository.saveAll(List.of(travel1, travel2));

        LocalDate requestDate = LocalDate.of(2024, 11, 6);

        given(userTagPreferenceRepository.findPreferenceTagsByUserNumber(any(Integer.class)))
                .willReturn(Arrays.asList("쇼핑", "자연"));

        // when
        Page<TravelRecommendDto> result = travelHomeService.getRecommendTravelsByUser(PageRequest.of(0, 5), 1, requestDate);

        // then
        assertThat(result.getContent()).hasSize(2)
                .extracting("travelNumber", "tags", "preferredNumber", "registerDue")
                .containsExactly(
                        tuple(travel2.getNumber(), Arrays.asList("쇼핑", "자연"), 2, dueDate),
                        tuple(travel1.getNumber(), Arrays.asList("쇼핑", "자연"), 2, dueDate.plusDays(1))
                );
    }

    @DisplayName("preferredNumber, registerDue가 동일한 경우 title을 기준으로 가나다 순서대로 가져온다.")
    @Test
    void getRecommendTravelsByUserWhenSamePreferredNumberSameRegisterDue() {
        Tag tag1 = Tag.of("쇼핑");
        Tag tag2 = Tag.of("자연");
        tagRepository.saveAll(List.of(tag1, tag2));

        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC));
        LocalDate dueDate = LocalDate.of(2024, 11, 7);
        Travel travel1 = createTravel(
                1, location, "여행-나", 0, 0, GenderType.MIXED,
                dueDate, PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag2));
        Travel travel2 = createTravel(
                1, location, "여행-가", 0, 0, GenderType.MIXED,
                dueDate, PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag2));
        travelRepository.saveAll(List.of(travel1, travel2));

        LocalDate requestDate = LocalDate.of(2024, 11, 6);

        given(userTagPreferenceRepository.findPreferenceTagsByUserNumber(any(Integer.class)))
                .willReturn(Arrays.asList("쇼핑", "자연"));

        // when
        Page<TravelRecommendDto> result = travelHomeService.getRecommendTravelsByUser(PageRequest.of(0, 5), 1, requestDate);

        // then
        assertThat(result.getContent()).hasSize(2)
                .extracting("travelNumber", "tags", "preferredNumber", "registerDue", "title")
                .containsExactly(
                        tuple(travel2.getNumber(), Arrays.asList("쇼핑", "자연"), 2, dueDate, "여행-가"),
                        tuple(travel1.getNumber(), Arrays.asList("쇼핑", "자연"), 2, dueDate, "여행-나")
                );
    }

    private Location createLocation(String locationName, LocationType locationType) {
        return Location.builder()
                .locationName(locationName)
                .locationType(locationType)
                .build();
    }

    private Travel createTravel(
            int hostNumber, Location location, String title, int viewCount, int maxPerson, GenderType genderType,
            LocalDate dueDate, PeriodType periodType, TravelStatus status, List<Tag> tags
    ) {
        return Travel.builder()
                .userNumber(hostNumber)
                .location(location)
                .locationName(location.getLocationName())
                .title(title)
                .details("여행 내용")
                .viewCount(viewCount)
                .maxPerson(maxPerson)
                .genderType(genderType)
                .dueDate(dueDate)
                .periodType(periodType)
                .status(status)
                .tags(tags)
                .build();
    }

}