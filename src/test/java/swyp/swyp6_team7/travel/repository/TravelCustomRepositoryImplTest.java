package swyp.swyp6_team7.travel.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import swyp.swyp6_team7.bookmark.entity.Bookmark;
import swyp.swyp6_team7.bookmark.repository.BookmarkRepository;
import swyp.swyp6_team7.config.DataConfig;
import swyp.swyp6_team7.location.domain.Continent;
import swyp.swyp6_team7.location.domain.Country;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.CountryRepository;
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
import swyp.swyp6_team7.travel.dto.TravelDetailDto;
import swyp.swyp6_team7.travel.dto.TravelRecommendForMemberDto;
import swyp.swyp6_team7.travel.dto.TravelRecommendForNonMemberDto;
import swyp.swyp6_team7.travel.dto.TravelSearchCondition;
import swyp.swyp6_team7.travel.dto.response.TravelRecentDto;
import swyp.swyp6_team7.travel.dto.response.TravelSearchDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static swyp.swyp6_team7.travel.domain.TravelStatus.*;

@Import(DataConfig.class)
@DataJpaTest
class TravelCustomRepositoryImplTest {

    @Autowired
    private TravelRepository travelRepository;
    @Autowired
    private TravelTagRepository travelTagRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private BookmarkRepository bookmarkRepository;


    @DisplayName("getDetailsByNumber: 여행 번호가 주어지면 여행의 상세 정보를 가져올 수 있다.")
    @Test
    public void getDetailsByNumber() {
        // given
        Users host = userRepository.save(createHostUser());

        Tag tag1 = Tag.of("쇼핑");
        Tag tag2 = Tag.of("자연");
        List<Tag> tags = tagRepository.saveAll(Arrays.asList(tag1, tag2));

        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));
        Travel travel1 = createTravel(
                host.getUserNumber(), location, "여행", 0, 2, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, tags);
        travelRepository.save(travel1);

        // when
        TravelDetailDto details = travelRepository.getDetailsByNumber(travel1.getNumber());

        // then
        assertThat(details)
                .extracting("travel", "hostNumber", "hostName", "hostAgeGroup", "companionCount", "tags")
                .contains(travel1, host.getUserNumber(), "주최자 이름", "10대", 0, List.of("쇼핑", "자연"));
    }

    @DisplayName("findAll: 생성 날짜가 최신 순서로 정렬된 여행 목록을 조회할 수 있다.")
    @Test
    public void findAllSortedByCreatedAt() {
        // given
        Users host = userRepository.save(createHostUser());
        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));

        Travel travel1 = createTravel(
                host.getUserNumber(), location, "여행", 0, 2, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.save(travel1);

        Travel travel2 = createTravel(
                host.getUserNumber(), location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.save(travel2);

        // when
        Page<TravelRecentDto> results = travelRepository
                .findAllSortedByCreatedAt(PageRequest.of(0, 5));

        // then
        assertThat(results.getContent()).hasSize(2)
                .extracting("travelNumber", "title", "location", "userNumber", "userName", "tags", "nowPerson", "maxPerson")
                .containsExactly(
                        tuple(travel2.getNumber(), "여행", "Seoul", host.getUserNumber(), "주최자 이름", List.of(), 0, 0),
                        tuple(travel1.getNumber(), "여행", "Seoul", host.getUserNumber(), "주최자 이름", List.of(), 0, 2)
                );
    }

    @DisplayName("findAllByPreferredTags: 사용자의 선호 태그가 많이 포함된 순서로 정렬된 여행 목록과 포함된 태그 개수를 함께 가져온다.")
    @Test
    public void findAllByPreferredTags() {
        // given
        Tag tag1 = Tag.of("쇼핑");
        Tag tag2 = Tag.of("자연");
        Tag tag3 = Tag.of("먹방");
        Tag tag4 = Tag.of("즉흥");
        tagRepository.saveAll(List.of(tag1, tag2, tag3, tag4));

        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));
        Travel travel1 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1));
        Travel travel2 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag2));
        Travel travel3 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag4));
        Travel travel4 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag2, tag3));
        travelRepository.saveAll(List.of(travel1, travel2, travel3, travel4));

        List<String> preferredTags = Arrays.asList("쇼핑", "자연", "먹방");
        LocalDate requestDate = LocalDate.of(2024, 11, 6);

        // when
        Page<TravelRecommendForMemberDto> result = travelRepository
                .findAllByPreferredTags(PageRequest.of(0, 5), 1, preferredTags, requestDate);

        // then
        assertThat(result.getContent()).hasSize(4)
                .extracting("travelNumber", "tags", "preferredNumber")
                .containsExactlyInAnyOrder(
                        tuple(travel1.getNumber(), Arrays.asList("쇼핑"), 1),
                        tuple(travel2.getNumber(), Arrays.asList("쇼핑", "자연"), 2),
                        tuple(travel3.getNumber(), Arrays.asList("쇼핑", "즉흥"), 1),
                        tuple(travel4.getNumber(), Arrays.asList("쇼핑", "자연", "먹방"), 3)
                );
    }

    @DisplayName("findAllByPreferredTags: 여행 시작 날짜가 지난 여행은 목록에 포함시키지 않는다.")
    @Test
    public void findAllByPreferredTagsWithDate() {
        // given
        Tag tag1 = Tag.of("쇼핑");
        Tag tag2 = Tag.of("자연");
        Tag tag3 = Tag.of("먹방");
        Tag tag4 = Tag.of("즉흥");
        tagRepository.saveAll(List.of(tag1, tag2, tag3, tag4));

        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));

        LocalDate startDate1 = LocalDate.of(2024, 11, 22);
        LocalDate endDate = LocalDate.of(2024, 11, 28);
        Travel travel1 = createTravelWithDate(
                1, location, startDate1, endDate, "여행", 0, 0,
                GenderType.MIXED, PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1));
        Travel travel2 = createTravelWithDate(
                1, location, startDate1, endDate, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag2));

        LocalDate startDate2 = LocalDate.of(2024, 11, 25);
        Travel travel3 = createTravelWithDate(
                1, location, startDate2, endDate, "여행", 0, 0,
                GenderType.MIXED, PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag4));
        Travel travel4 = createTravelWithDate(
                1, location, startDate2, endDate, "여행", 0, 0,
                GenderType.MIXED, PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag2, tag3));
        travelRepository.saveAll(List.of(travel1, travel2, travel3, travel4));

        List<String> preferredTags = Arrays.asList("쇼핑", "자연", "먹방");
        LocalDate requestDate = LocalDate.of(2024, 11, 24);

        // when
        Page<TravelRecommendForMemberDto> result = travelRepository
                .findAllByPreferredTags(PageRequest.of(0, 5), 1, preferredTags, requestDate);

        // then
        assertThat(result.getContent()).hasSize(2)
                .extracting("travelNumber", "tags", "preferredNumber")
                .containsExactlyInAnyOrder(
                        tuple(travel3.getNumber(), Arrays.asList("쇼핑", "즉흥"), 1),
                        tuple(travel4.getNumber(), Arrays.asList("쇼핑", "자연", "먹방"), 3)
                );
    }

    @DisplayName("findAllByBookmarkNumberAndTitle: 북마크 개수가 많은 순서로 정렬된 여행 목록을 가져온다.")
    @Test
    void findAllByBookmarkNumberAndTitle() {
        // given
        Users host = userRepository.save(createHostUser());
        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));
        Travel travel1 = createTravel(
                host.getUserNumber(), location, "여행1", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel2 = createTravel(
                host.getUserNumber(), location, "여행2", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2));

        LocalDateTime createdAt = LocalDateTime.of(2024, 12, 7, 12, 0);
        Bookmark bookmark1 = createBookmark(travel1.getNumber(), createdAt);
        Bookmark bookmark2 = createBookmark(travel2.getNumber(), createdAt);
        Bookmark bookmark3 = createBookmark(travel2.getNumber(), createdAt);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        LocalDate requestDate = LocalDate.of(2024, 11, 8);

        // when
        Page<TravelRecommendForNonMemberDto> result = travelRepository.findAllSortedByBookmarkNumberAndTitle(PageRequest.of(0, 5), requestDate);

        // then
        assertThat(result.getContent()).hasSize(2)
                .extracting("travelNumber", "title", "bookmarkCount",
                        "location", "userNumber", "userName", "tags", "nowPerson", "maxPerson")
                .containsExactly(
                        tuple(travel2.getNumber(), "여행2", 2,
                                "Seoul", host.getUserNumber(), "주최자 이름", List.of(), 0, 0),
                        tuple(travel1.getNumber(), "여행1", 1,
                                "Seoul", host.getUserNumber(), "주최자 이름", List.of(), 0, 0)
                );
    }

    @DisplayName("findAllByBookmarkNumberAndTitle: 북마크 개수가 같으면 제목 사전순으로 추가 정렬한다.")
    @Test
    void findAllByBookmarkNumberAndTitleWhenSameBookmarkNumber() {
        // given
        Users host = userRepository.save(createHostUser());
        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));
        Travel travel1 = createTravel(
                host.getUserNumber(), location, "여행갸", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel2 = createTravel(
                host.getUserNumber(), location, "여행가", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2));

        LocalDateTime createdAt = LocalDateTime.of(2024, 12, 7, 12, 0);
        Bookmark bookmark1 = createBookmark(travel1.getNumber(), createdAt);
        Bookmark bookmark2 = createBookmark(travel2.getNumber(), createdAt);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2));

        LocalDate requestDate = LocalDate.of(2024, 11, 8);

        // when
        Page<TravelRecommendForNonMemberDto> result = travelRepository.findAllSortedByBookmarkNumberAndTitle(PageRequest.of(0, 5), requestDate);

        // then
        assertThat(result.getContent()).hasSize(2)
                .extracting("travelNumber", "title", "bookmarkCount",
                        "location", "userNumber", "userName", "tags", "nowPerson", "maxPerson")
                .containsExactly(
                        tuple(travel2.getNumber(), "여행가", 1,
                                "Seoul", host.getUserNumber(), "주최자 이름", List.of(), 0, 0),
                        tuple(travel1.getNumber(), "여행갸", 1,
                                "Seoul", host.getUserNumber(), "주최자 이름", List.of(), 0, 0)
                );
    }

    @DisplayName("findAllByBookmarkNumberAndTitle: 여행 시작 날짜가 지난 여행은 목록에 포함시키지 않는다.")
    @Test
    void findAllByBookmarkNumberAndTitleWithDate() {
        // given
        Users host = userRepository.save(createHostUser());
        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));

        LocalDate startDate1 = LocalDate.of(2024, 11, 22);
        LocalDate endDate = LocalDate.of(2024, 11, 28);
        Travel travel1 = createTravelWithDate(
                host.getUserNumber(), location, startDate1, endDate, "여행1", 0, 0,
                GenderType.MIXED, PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());

        LocalDate startDate2 = LocalDate.of(2024, 11, 25);
        Travel travel2 = createTravelWithDate(
                host.getUserNumber(), location, startDate2, endDate, "여행2", 0, 0,
                GenderType.MIXED, PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2));

        LocalDateTime createdAt = LocalDateTime.of(2024, 12, 7, 12, 0);
        Bookmark bookmark1 = createBookmark(travel1.getNumber(), createdAt);
        Bookmark bookmark2 = createBookmark(travel2.getNumber(), createdAt);
        Bookmark bookmark3 = createBookmark(travel2.getNumber(), createdAt);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        LocalDate requestDate = LocalDate.of(2024, 11, 24);

        // when
        Page<TravelRecommendForNonMemberDto> result = travelRepository.findAllSortedByBookmarkNumberAndTitle(PageRequest.of(0, 5), requestDate);

        // then
        assertThat(result.getContent()).hasSize(1)
                .extracting("travelNumber", "title", "bookmarkCount",
                        "location", "userNumber", "userName", "tags", "nowPerson", "maxPerson")
                .containsExactly(
                        tuple(travel2.getNumber(), "여행2", 2,
                                "Seoul", host.getUserNumber(), "주최자 이름", List.of(), 0, 0)
                );
    }

    @DisplayName("search: 제목에 keyword가 포함된 여행 목록을 가져올 수 있다.")
    @Test
    public void searchWithKeyword() {
        // given
        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));
        Travel travel1 = createTravel(
                1, location, "Seoul 여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel2 = createTravel(
                1, location, "키워드", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .keyword("키워드")
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition);

        // then
        assertThat(results.getContent()).hasSize(1)
                .extracting("title")
                .allMatch(title -> ((String) title).contains("키워드"));
    }

    @DisplayName("search: 장소 이름에 keyword가 포함된 여행 목록을 가져올 수 있다.")
    @Test
    public void searchWithKeywordThroughTitleAndLocation() {
        // given
        Country country = createCountry("대한민국",Continent.ASIA);

        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));
        Location location2 = locationRepository.save(createLocation("Busan", LocationType.DOMESTIC,country));
        Travel travel1 = createTravel(
                1, location, "여행1", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel2 = createTravel(
                1, location2, "여행2", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .keyword("Seoul")
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition);

        // then
        assertThat(results.getContent()).hasSize(1)
                .extracting("location")
                .allMatch(locationName -> ((String) locationName).contains("Seoul"));
    }


    @DisplayName("search: 검색 키워드가 주어지지 않으면 필터링 조건으로 작용하지 않는다.")
    @Test
    public void searchWithoutKeyword() {
        // given
        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));
        Travel travel1 = createTravel(
                1, location, "Seoul 여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel2 = createTravel(
                1, location, "키워드", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition);

        // then
        assertThat(results.getContent()).hasSize(2);
    }

    @DisplayName("search: 콘텐츠의 상태가 IN_PROGRESS, CLOSED인 여행만 검색 결과에 포함되어 있다.")
    @Test
    public void searchOnlyActivated() {
        // given
        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));
        Travel travel1 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, DELETED, new ArrayList<>());
        Travel travel2 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel3 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, CLOSED, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2, travel3));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition);

        // then
        assertThat(results.getContent()).hasSize(2)
                .extracting("postStatus")
                .allMatch(status -> List.of(IN_PROGRESS.toString(), CLOSED.toString()).contains(status));
    }

    @DisplayName("search: 여러 개의 태그 이름이 주어질 때, 태그를 전부 포함하는 여행 목록을 가져온다.")
    @Test
    public void searchWithTags() {
        // given
        Tag tag1 = Tag.of("쇼핑");
        Tag tag2 = Tag.of("자연");
        tagRepository.saveAll(Arrays.asList(tag1, tag2));

        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));
        Travel travel1 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1, tag2));
        Travel travel2 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, Arrays.asList(tag1));
        travelRepository.saveAll(List.of(travel1, travel2));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .tags(List.of("쇼핑", "자연"))
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition);

        // then
        assertThat(result.getContent()).hasSize(1)
                .allSatisfy(dto -> assertThat(dto.getTags())
                        .containsExactlyInAnyOrder("자연", "쇼핑")
                );
    }

    @DisplayName("search: 주어지는 GenderType을 가진 여행 목록을 가져올 수 있다.")
    @Test
    public void searchWithGenderFilter() {
        // given
        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));
        Travel travel1 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel2 = createTravel(
                1, location, "여행", 0, 0, GenderType.WOMAN_ONLY,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel3 = createTravel(
                1, location, "여행", 0, 0, GenderType.MAN_ONLY,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2, travel3));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .genderTypes(List.of("모두", "여자만"))
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition);

        // then
        assertThat(result.getContent()).hasSize(2)
                .extracting("travelNumber")
                .containsExactlyInAnyOrder(travel1.getNumber(), travel2.getNumber());
    }

    @DisplayName("search: 주어지는 PeriodType을 가진 여행 목록을 가져올 수 있다.")
    @Test
    public void searchWithPeriodFilter() {
        // given
        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));
        Travel travel1 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.MORE_THAN_MONTH, IN_PROGRESS, new ArrayList<>());
        Travel travel2 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.THREE_WEEKS, IN_PROGRESS, new ArrayList<>());
        Travel travel3 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.TWO_WEEKS, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2, travel3));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .periodTypes(List.of("한 달 이상", "3~4주"))
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition);

        // then
        assertThat(result.getContent()).hasSize(2)
                .extracting("travelNumber")
                .containsExactlyInAnyOrder(travel1.getNumber(), travel2.getNumber());
    }


    @DisplayName("search: 주어지는 personTypes을 가진 여행을 가져올 수 있다.")
    @Test
    public void searchWithPersonRangeFilter() {
        // given
        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));
        Travel travel1 = createTravel(
                1, location, "여행", 0, 2, GenderType.MIXED,
                PeriodType.MORE_THAN_MONTH, IN_PROGRESS, new ArrayList<>());
        Travel travel2 = createTravel(
                1, location, "여행", 0, 3, GenderType.MIXED,
                PeriodType.THREE_WEEKS, IN_PROGRESS, new ArrayList<>());
        Travel travel3 = createTravel(
                1, location, "여행", 0, 4, GenderType.MIXED,
                PeriodType.TWO_WEEKS, IN_PROGRESS, new ArrayList<>());
        Travel travel4 = createTravel(
                1, location, "여행", 0, 5, GenderType.MIXED,
                PeriodType.TWO_WEEKS, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2, travel3, travel4));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .personTypes(List.of("3~4명"))
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition);

        // then
        assertThat(result.getContent()).hasSize(2)
                .extracting("travelNumber")
                .containsExactlyInAnyOrder(travel2.getNumber(), travel3.getNumber());
    }


    @DisplayName("search: 국내 카테고리(DOMESTIC)에 해당하는 여행을 검색할 수 있다.")
    @Test
    public void searchDomesticTravelWithLocationFilter() {
        Country korea = createCountry("대한민국",Continent.ASIA);
        Country england = createCountry("영국",Continent.EUROPE);

        Location domesticLocation = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,korea));
        Location internationalLocation = locationRepository.save(createLocation("London", LocationType.INTERNATIONAL,england));

        LocalDate dueDate = LocalDate.of(2024, 11, 7);
        Travel travel1 = createTravel(
                1, domesticLocation, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel2 = createTravel(
                1, domesticLocation, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel3 = createTravel(
                1, internationalLocation, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2, travel3));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .locationTypes(List.of("국내"))
                .build();

        // when
        Page<TravelSearchDto> domesticResults = travelRepository.search(condition);

        // then
        assertThat(domesticResults.getContent()).hasSize(2)
                .extracting("travelNumber")
                .containsExactlyInAnyOrder(travel1.getNumber(), travel2.getNumber());
    }

    @DisplayName("search: 해외 카테고리(INTERNATIONAL)에 해당하는 여행을 검색할 수 있다.")
    @Test
    public void searchInternationalTravelWithLocationFilter() {
        // given
        Country korea = createCountry("대한민국",Continent.ASIA);
        Country england = createCountry("영국",Continent.EUROPE);

        Location domesticLocation = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC, korea));
        Location internationalLocation = locationRepository.save(createLocation("London", LocationType.INTERNATIONAL, england));

        Travel travel1 = createTravel(
                1, domesticLocation, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel2 = createTravel(
                1, internationalLocation, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel3 = createTravel(
                1, internationalLocation, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2, travel3));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .locationTypes(List.of("해외"))
                .build();

        // when
        Page<TravelSearchDto> domesticResults = travelRepository.search(condition);

        // then
        assertThat(domesticResults.getContent()).hasSize(2)
                .extracting("travelNumber")
                .containsExactlyInAnyOrder(travel2.getNumber(), travel3.getNumber());
    }

    @DisplayName("search: 검색 정렬 조건이 주어지지 않으면, 최신 생성 순서로 정렬된 여행 목록을 가져온다.")
    @Test
    public void searchWithoutSortingType() {
        // given
        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC, country));
        Travel travel1 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel2 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel3 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2, travel3));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition);

        // then
        assertThat(results.getContent()).hasSize(3)
                .extracting("travelNumber")
                .containsExactly(travel3.getNumber(), travel2.getNumber(), travel1.getNumber());
    }

    @DisplayName("search: 검색의 추천순 정렬은 북마크 개수, 조회수가 많은 순서대로 정렬된 여행 목록을 가져온다.")
    @Test
    public void searchWithRecommendSorting() {
        // given
        Country country = createCountry("대한민국",Continent.ASIA);

        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC, country));
        Travel travel1 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel2 = createTravel(
                1, location, "여행", 3, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel3 = createTravel(
                1, location, "여행", 2, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2, travel3));

        LocalDateTime createdAt = LocalDateTime.of(2024, 11, 6, 12, 0);
        Bookmark bookmark1 = createBookmark(travel2.getNumber(), createdAt);
        Bookmark bookmark2 = createBookmark(travel3.getNumber(), createdAt);
        Bookmark bookmark3 = createBookmark(travel3.getNumber(), createdAt);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .sortingType("추천순")
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition);

        // then
        assertThat(results.getContent()).hasSize(3)
                .extracting("travelNumber")
                .containsExactly(travel3.getNumber(), travel2.getNumber(), travel1.getNumber());
    }

    @DisplayName("search: 검색 추천순 정렬에서 북마크 개수가 동일하면, 조회수가 많은 순서대로 추가 정렬한다.")
    @Test
    void searchWithRecommendSorting2() {
        // given
        Country country = createCountry("대한민국",Continent.ASIA);
        Location location = locationRepository.save(createLocation("Seoul", LocationType.DOMESTIC,country));
        Travel travel1 = createTravel(
                1, location, "여행", 0, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel2 = createTravel(
                1, location, "여행", 3, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        Travel travel3 = createTravel(
                1, location, "여행", 1, 0, GenderType.MIXED,
                PeriodType.ONE_WEEK, IN_PROGRESS, new ArrayList<>());
        travelRepository.saveAll(List.of(travel1, travel2, travel3));

        LocalDateTime createdAt = LocalDateTime.of(2024, 11, 6, 12, 0);
        Bookmark bookmark1 = createBookmark(travel1.getNumber(), createdAt);
        Bookmark bookmark2 = createBookmark(travel2.getNumber(), createdAt);
        Bookmark bookmark3 = createBookmark(travel3.getNumber(), createdAt);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .sortingType("추천순")
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition);

        // then
        assertThat(results.getContent()).hasSize(3)
                .extracting("travelNumber")
                .containsExactly(travel2.getNumber(), travel3.getNumber(), travel1.getNumber());
    }


    private Users createHostUser() {
        return Users.builder()
                .userNumber(1)
                .userPw("1234")
                .userEmail("test@mail.com")
                .userName("주최자 이름")
                .userGender(Gender.M)
                .userAgeGroup(AgeGroup.TEEN)
                .userStatus(UserStatus.ABLE)
                .build();
    }

    private Country createCountry(String name, Continent continent){
        return countryRepository.save(Country.builder()
                .countryName(name)
                .continent(continent)
                .build());
    }

    private Location createLocation(String locationName, LocationType locationType, Country country) {
        return Location.builder()
                .locationName(locationName)
                .locationType(locationType)
                .country(country)
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

    private Travel createTravelWithDate(
            int hostNumber, Location location, LocalDate startDate, LocalDate endDate, String title, int viewCount,
            int maxPerson, GenderType genderType, PeriodType periodType, TravelStatus status, List<Tag> tags
    ) {
        return Travel.builder()
                .userNumber(hostNumber)
                .location(location)
                .locationName(location.getLocationName())
                .startDate(startDate)
                .endDate(endDate)
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

    private Bookmark createBookmark(int travelNumber, LocalDateTime createdAt) {
        return Bookmark.builder()
                .userNumber(1)
                .travelNumber(travelNumber)
                .bookmarkDate(createdAt)
                .build();
    }

}

