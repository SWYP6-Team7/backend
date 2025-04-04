package swyp.swyp6_team7.travel.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import swyp.swyp6_team7.bookmark.service.BookmarkService;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.repository.TagRepository;
import swyp.swyp6_team7.tag.repository.TravelTagRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.TravelSearchCondition;
import swyp.swyp6_team7.travel.dto.response.TravelSearchDto;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static swyp.swyp6_team7.travel.domain.TravelStatus.IN_PROGRESS;

@SpringBootTest
@Disabled
class TravelSearchServiceTest {

    @Autowired
    private TravelSearchService travelSearchService;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private TravelTagRepository travelTagRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private BookmarkService bookmarkService;


    @AfterEach
    void tearDown() {
        travelTagRepository.deleteAllInBatch();
        travelRepository.deleteAllInBatch();
        tagRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("search: 비로그인 사용자는 여행을 검색할 수 있다.")
    @Test
    void searchWhenNotLogin() {
        // given
        Integer hostUserNumber = userRepository.save(createHostUser()).getUserNumber();
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC));
        Travel travel1 = travelRepository.save(createTravel(
                hostUserNumber, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList()));
        Travel travel2 = travelRepository.save(createTravel(
                hostUserNumber, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList()));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> result = travelSearchService.search(condition, null);

        // then
        assertThat(result).hasSize(2)
                .extracting("travelNumber", "title", "location", "userNumber", "userName",
                        "tags", "nowPerson", "maxPerson", "postStatus", "bookmarked")
                .containsExactly(
                        tuple(travel2.getNumber(), "여행", "Seoul", hostUserNumber, "주최자명",
                                List.of(), 0, 0, IN_PROGRESS.getName(), false),
                        tuple(travel1.getNumber(), "여행", "Seoul", hostUserNumber, "주최자명",
                                List.of(), 0, 0, IN_PROGRESS.getName(), false)
                );
    }

    @DisplayName("search: 로그인 사용자는 여행을 검색하고 해당 여행에 대한 북마크 여부까지 알 수 있다.")
    @Test
    void search() {
        // given
        Integer hostUserNumber = userRepository.save(createHostUser()).getUserNumber();
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC));
        Travel travel1 = travelRepository.save(createTravel(
                hostUserNumber, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList()));
        Travel travel2 = travelRepository.save(createTravel(
                hostUserNumber, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList()));

        Map<Integer, Boolean> bookmarkedMap = new HashMap<>();
        bookmarkedMap.put(travel1.getNumber(), true);
        bookmarkedMap.put(travel2.getNumber(), false);
        given(bookmarkService.getBookmarkExistenceByTravelNumbers(anyInt(), anyList()))
                .willReturn(bookmarkedMap);

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .build();
        Integer loginUserNumber = 10;

        // when
        Page<TravelSearchDto> result = travelSearchService.search(condition, loginUserNumber);

        // then
        then(bookmarkService).should(times(1))
                .getBookmarkExistenceByTravelNumbers(10, List.of(travel2.getNumber(), travel1.getNumber()));
        assertThat(result).hasSize(2)
                .extracting("travelNumber", "title", "location", "userNumber", "userName",
                        "tags", "nowPerson", "maxPerson", "postStatus", "bookmarked")
                .containsExactly(
                        tuple(travel2.getNumber(), "여행", "Seoul", hostUserNumber, "주최자명",
                                List.of(), 0, 0, IN_PROGRESS.getName(), false),
                        tuple(travel1.getNumber(), "여행", "Seoul", hostUserNumber, "주최자명",
                                List.of(), 0, 0, IN_PROGRESS.getName(), true)
                );
    }

    private Users createHostUser() {
        return Users.builder()
                .userPw("1234")
                .userEmail("test@mail.com")
                .userName("주최자명")
                .userGender(Gender.M)
                .userAgeGroup(AgeGroup.TEEN)
                .userStatus(UserStatus.ABLE)
                .build();
    }

    private Location createLocation(String locationName, LocationType locationType) {
        return Location.builder()
                .locationName(locationName)
                .locationType(locationType)
                .build();
    }

    private Travel createTravel(
            int hostNumber, Location location, String title, int viewCount, int maxPerson,
            GenderType genderType, PeriodType periodType, TravelStatus status, List<Tag> tags
    ) {
        return Travel.builder()
                .userNumber(hostNumber)
                .location(location)
                .locationName(location.getLocationName())
                .startDate(LocalDate.of(2024, 11, 22))
                .endDate(LocalDate.of(2024, 11, 28))
                .title(title)
                .details("여행 내용")
                .viewCount(viewCount)
                .maxPerson(maxPerson)
                .genderType(genderType)
                .periodType(periodType)
                .status(status)
                .tags(tags)
                .build();
    }
}
