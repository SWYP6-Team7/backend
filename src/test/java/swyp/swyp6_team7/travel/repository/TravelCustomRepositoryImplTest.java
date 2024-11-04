package swyp.swyp6_team7.travel.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import swyp.swyp6_team7.config.DataConfig;
import swyp.swyp6_team7.enrollment.domain.Enrollment;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.enrollment.repository.EnrollmentCustomRepository;
import swyp.swyp6_team7.enrollment.repository.EnrollmentRepository;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.domain.TravelTag;
import swyp.swyp6_team7.tag.repository.TagRepository;
import swyp.swyp6_team7.tag.repository.TravelTagRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.TravelDetailDto;
import swyp.swyp6_team7.travel.dto.TravelRecommendDto;
import swyp.swyp6_team7.travel.dto.TravelSearchCondition;
import swyp.swyp6_team7.travel.dto.response.TravelRecentDto;
import swyp.swyp6_team7.travel.dto.response.TravelSearchDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


import swyp.swyp6_team7.enrollment.domain.Enrollment;

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
    @Qualifier("enrollmentCustomRepositoryImpl")
    private EnrollmentCustomRepository enrollmentCustomRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private LocationRepository locationRepository;

    Users user;

    @BeforeEach
    void setUp() {

        travelTagRepository.deleteAll();
        travelRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();
        enrollmentRepository.deleteAll();

        Location travelLocation = Location.builder()
                .locationName("Seoul")
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        Travel savedTravel = travelRepository.save(Travel.builder()
                .title("기본 테스트 데이터")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());

        user = userRepository.save(Users.builder()
                .userNumber(1)
                .userEmail("testuser@naver.com")
                .userPw("password")
                .userName("테스트 사용자")
                .userGender(Gender.M)
                .userAgeGroup(AgeGroup.TWENTY)
                .userRegDate(LocalDateTime.now())
                .userStatus(UserStatus.ABLE)
                .build());

        int travelNumber = savedTravel.getNumber();

        enrollmentRepository.save(Enrollment.builder()
                .userNumber(user.getUserNumber())
                .travelNumber(travelNumber)
                .createdAt(LocalDateTime.now())
                .message("참가 신청 메시지")
                .status(EnrollmentStatus.ACCEPTED)
                .build());
    }

    @AfterEach
    void tearDown() {
        // 데이터 삭제
        travelTagRepository.deleteAllInBatch();
        travelRepository.deleteAllInBatch();
        tagRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        enrollmentRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
    }

    @DisplayName("getDetailsByNumber: 여행콘텐츠 식별자로 디테일 정보를 가져온다")
    @Test
    public void getDetailsByNumber() {
        // given
        Location travelLocation = Location.builder()
                .locationName("Seoul" + System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        Users user = userRepository.save(Users.builder()
                .userEmail("test@naver.com")
                .userPw("1234")
                .userName("모잉")
                .userGender(Gender.M)
                .userAgeGroup(AgeGroup.TWENTY)
                .userRegDate(LocalDateTime.now())
                .userStatus(UserStatus.ABLE)
                .build());
        Tag tag1 = tagRepository.save(Tag.of("한국"));
        Tag tag2 = tagRepository.save(Tag.of("투어"));
        Travel travel = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터1")
                .userNumber(user.getUserNumber())
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        travelTagRepository.save(TravelTag.of(travel, tag1));
        travelTagRepository.save(TravelTag.of(travel, tag2));

        // when
        TravelDetailDto details = travelRepository.getDetailsByNumber(travel.getNumber(), user.getUserNumber());

        // then
        System.out.println(details.toString());
        assertThat(details.getTravel().getTitle()).isEqualTo("추가 테스트 데이터1");
        assertThat(details.getHostNumber()).isEqualTo(user.getUserNumber());
        assertThat(details.getHostName()).isEqualTo("모잉");
        assertThat(details.getTags().size()).isEqualTo(2);
    }

    @DisplayName("findAll: 여행 콘텐츠를 DTO로 만들어 최신순으로 정렬해 반환한다")
    @Test
    public void findAllSortedByCreatedAt() {
        // given
        Location travelLocation = Location.builder()
                .locationName("Seoul" + System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        Travel travel = Travel.builder()
                .title("추가 테스트 데이터")
                .userNumber(1)
                .viewCount(0)
                .location(savedLocation)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now().plusDays(1))
                .status(TravelStatus.IN_PROGRESS)
                .build();
        travelRepository.save(travel);
        for (int i = 0; i < 5; i++) {
            Tag tag = tagRepository.save(Tag.of("태그" + i));
            travelTagRepository.save(TravelTag.of(travel, tag));
        }

        // when
        Page<TravelRecentDto> results = travelRepository
                .findAllSortedByCreatedAt(PageRequest.of(0, 5), 1);

        // then
        for (TravelRecentDto result : results) {
            System.out.println(result.toString());
        }
        assertThat(results.getContent().size()).isEqualTo(2);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("추가 테스트 데이터");
        assertThat(results.getContent().get(0).getTags().size()).isEqualTo(5);
    }

    @DisplayName("findAll: 최신순으로 정렬해 반환할 때 데이터가 없을 경우에도 오류가 나지 않는다")
    @Test
    public void findAllSortedByCreatedAtNoData() {
        // given
        travelRepository.deleteAll();
        Location travelLocation = Location.builder()
                .locationName("Seoul" + System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);

        // when
        Page<TravelRecentDto> results = travelRepository
                .findAllSortedByCreatedAt(PageRequest.of(0, 5), 1);

        // then
        for (TravelRecentDto result : results) {
            System.out.println(result.toString());
        }
        assertThat(results.getContent().size()).isEqualTo(0);
    }

    @DisplayName("findAllByPreferredTags: 사용자의 선호 태그와 콘텐츠 태그의 중복이 많은 순서대로 콘텐츠를 정렬한다")
    @Test
    public void findAllByPreferredTags() {
        // given
        //travelRepository.deleteAll();
        Location travelLocation = Location.builder()
                .locationName("Seoul" + System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        Tag tag1 = tagRepository.save(Tag.of("한국"));
        Tag tag2 = tagRepository.save(Tag.of("투어"));
        Tag tag3 = tagRepository.save(Tag.of("도시"));
        Tag tag4 = tagRepository.save(Tag.of("여유"));
        List<String> preferredTags = new ArrayList<>(List.of("한국", "투어", "도시"));


        Travel travel2 = travelRepository.save(Travel.builder()
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDate.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        //tags: 한국 -> 1개
        travelTagRepository.save(TravelTag.of(travel2, tag1));

        Travel travel3 = travelRepository.save(Travel.builder()
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        //tags: 한국, 투어 -> 2개
        travelTagRepository.save(TravelTag.of(travel3, tag1));
        travelTagRepository.save(TravelTag.of(travel3, tag2));

        Travel travel4 = travelRepository.save(Travel.builder()
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDate.now().plusDays(1))
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        //tags: 한국, 여유 -> 1개
        travelTagRepository.save(TravelTag.of(travel4, tag1));
        travelTagRepository.save(TravelTag.of(travel4, tag4));

        Travel travel5 = travelRepository.save(Travel.builder()
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        //tags: 한국, 투어, 도시 -> 3개
        travelTagRepository.save(TravelTag.of(travel5, tag1));
        travelTagRepository.save(TravelTag.of(travel5, tag2));
        travelTagRepository.save(TravelTag.of(travel5, tag3));

        // when
        Page<TravelRecommendDto> result = travelRepository
                .findAllByPreferredTags(PageRequest.of(0, 5), user.getUserNumber(), preferredTags);

        // then
        assertThat(result.getContent().size()).isEqualTo(5); //0, 1, 2, 1, 3
        List<Integer> preferredNum = result.stream()
                .map(dto -> dto.getPreferredNumber())
                .toList();
        assertThat(Collections.frequency(preferredNum, 0)).isEqualTo(1);
        assertThat(Collections.frequency(preferredNum, 1)).isEqualTo(2);
        assertThat(Collections.frequency(preferredNum, 2)).isEqualTo(1);
        assertThat(Collections.frequency(preferredNum, 3)).isEqualTo(1);
    }

    @DisplayName("search: 제목에 keyword가 포함된 데이터를 찾을 수 있다")
    @Test
    public void searchWithKeyword() {
        // given
        Location travelLocation = Location.builder()
                .locationName("Seoul" + System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        Travel travel = Travel.builder()
                .title("추가 테스트 데이터")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build();
        travelRepository.save(travel);

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .keyword("추가")
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition, user.getUserNumber());

        // then
        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent().size()).isEqualTo(1);
    }

    @DisplayName("search: 제목과 장소에 keyword가 포함된 데이터를 찾을 수 있다")
    @Test
    public void searchWithKeywordThroughTitleAndLocation() {
        // given
        Location travelLocation = Location.builder()
                .locationName("영국" + System.currentTimeMillis())
                .locationType(LocationType.INTERNATIONAL)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        Travel travel = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터")
                .userNumber(1)
                .viewCount(0)
                .locationName("영국" + System.currentTimeMillis())
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        Travel travel2 = travelRepository.save(Travel.builder()
                .title("영국 테스트 데이터")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .keyword("영국")
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition, user.getUserNumber());

        // then
        assertThat(results.getTotalElements()).isEqualTo(2);
        assertThat(results.getContent().size()).isEqualTo(2);
    }

    @DisplayName("search: 키워드가 주어지지 않을 경우 가능한 모든 콘텐츠를 전달")
    @Test
    public void searchWithoutKeyword() {
        // given
        Location travelLocation = Location.builder()
                .locationName("Seoul" + System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        Travel travel = Travel.builder()
                .title("추가 테스트 데이터")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build();
        travelRepository.save(travel);

        TravelSearchCondition condition = TravelSearchCondition.builder()
                //.keyword("")
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition, user.getUserNumber());

        // then
        assertThat(results.getTotalElements()).isEqualTo(2);
        assertThat(results.getContent().size()).isEqualTo(2);
    }

    @DisplayName("search: 콘텐츠의 상태가 DELETED, DRAFT일 경우 제외하고 가져온다")
    @Test
    public void searchOnlyActivated() {
        // given
        Location travelLocation = Location.builder()
                .locationName("Seoul" + System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        Travel deletedTravel = Travel.builder()
                .title("추가 테스트 데이터1")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.DELETED)
                .location(savedLocation)
                .build();
        travelRepository.save(deletedTravel);
        Travel draftTravel = Travel.builder()
                .title("추가 테스트 데이터2")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.DELETED)
                .location(savedLocation)
                .build();
        travelRepository.save(draftTravel);

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .keyword("데이터")
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition, user.getUserNumber());

        // then
        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent().size()).isEqualTo(1);
    }

    @DisplayName("search: 페이징을 이용해 콘텐츠를 가져올 수 있다")
    @Test
    public void searchWithPaging() {
        // given
        Location travelLocation = Location.builder()
                .locationName("Seoul" + System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        for (int i = 0; i < 6; i++) {
            travelRepository.save(Travel.builder()
                    .title("추가 테스트 데이터")
                    .userNumber(1)
                    .viewCount(0)
                    .periodType(PeriodType.NONE)
                    .genderType(GenderType.NONE)
                    .createdAt(LocalDateTime.now())
                    .status(TravelStatus.IN_PROGRESS)
                    .location(savedLocation)
                    .build());
        }

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .keyword("추가")
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition, user.getUserNumber());

        // then
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getContent().size()).isEqualTo(5);
    }

    @DisplayName("search: 주어지는 태그가 달린 콘텐츠를 가져온다")
    @Test
    public void searchWithTags() {
        // given
        Tag tag = tagRepository.save(Tag.of("테스트"));
        Location travelLocation = Location.builder()
                .locationName("Seoul" + System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        for (int i = 0; i < 6; i++) {
            Travel travel = travelRepository.save(Travel.builder()
                    .title("추가 테스트 데이터" + i)
                    .userNumber(1)
                    .viewCount(0)
                    .periodType(PeriodType.NONE)
                    .genderType(GenderType.NONE)
                    .createdAt(LocalDateTime.now())
                    .status(TravelStatus.IN_PROGRESS)
                    .location(savedLocation)
                    .build());
            travelTagRepository.save(TravelTag.of(travel, tag));
        }

        List<String> tags = new ArrayList<>();
        tags.add("테스트");
        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .tags(tags)
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition, user.getUserNumber());

        // then;
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getContent().size()).isEqualTo(5);
    }

    @DisplayName("search: 여러 개의 태그가 주어졌을 때, 모든 태그를 가진 콘텐츠만 가져온다")
    @Test
    public void searchWithMultipleTags() {
        // given
        Tag tag1 = tagRepository.save(Tag.of("한국"));
        Tag tag2 = tagRepository.save(Tag.of("투어"));
        Tag tag3 = tagRepository.save(Tag.of("도시"));
        Location travelLocation = Location.builder()
                .locationName("Seoul" + System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);

        Travel travel1 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터1")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        //tags: 한국, 도시
        travelTagRepository.save(TravelTag.of(travel1, tag1));
        travelTagRepository.save(TravelTag.of(travel1, tag3));

        Travel travel2 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터2")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        //tags: 한국, 투어
        travelTagRepository.save(TravelTag.of(travel2, tag1));
        travelTagRepository.save(TravelTag.of(travel2, tag2));

        List<String> tags = new ArrayList<>();
        tags.add("한국");
        tags.add("투어");
        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .tags(tags)
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition, user.getUserNumber());

        // then
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("추가 테스트 데이터2");
    }

    @DisplayName("search: 주어지는 젠더 타입에 알맞은 콘텐츠를 가져올 수 있다")
    @Test
    public void searchWithGenderFilter() {
        // given
        Location travelLocation = Location.builder()
                .locationName("Seoul" + System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        Travel travel1 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터1")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.WOMAN_ONLY)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        Travel travel2 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터2")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.MIXED)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        Travel travel3 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터3")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.MAN_ONLY)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .genderTypes(new ArrayList<>(List.of(GenderType.WOMAN_ONLY.toString(), GenderType.MIXED.toString())))
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition, user.getUserNumber());

        // then
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getContent().stream().map(c -> c.getTravelNumber())).contains(travel1.getNumber());
        assertThat(result.getContent().stream().map(c -> c.getTravelNumber())).contains(travel2.getNumber());
    }

    @DisplayName("search: 주어지는 기간 타입에 알맞은 콘텐츠를 가져올 수 있다")
    @Test
    public void searchWithPeriodFilter() {
        // given
        Location travelLocation = Location.builder()
                .locationName("Seoul" + System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        Travel travel1 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터1")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.MORE_THAN_MONTH)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        Travel travel2 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터2")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.THREE_WEEKS)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        Travel travel3 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터3")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.TWO_WEEKS)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .periodTypes(new ArrayList<>(List.of(
                        PeriodType.MORE_THAN_MONTH.toString(),
                        PeriodType.THREE_WEEKS.toString()))
                )
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition, user.getUserNumber());

        // then
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getContent().stream().map(c -> c.getTravelNumber())).contains(travel1.getNumber());
        assertThat(result.getContent().stream().map(c -> c.getTravelNumber())).contains(travel2.getNumber());
    }

    @DisplayName("search: 주어지는 인원 타입에 알맞은 콘텐츠를 가져올 수 있다")
    @Test
    public void searchWithPersonRangeFilter() {
        // given
        travelRepository.deleteAll();
        Location travelLocation = Location.builder()
                .locationName("Seoul" + System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        Travel travel1 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터1")
                .userNumber(1)
                .viewCount(0)
                .maxPerson(1)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        Travel travel2 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터2")
                .userNumber(1)
                .viewCount(0)
                .maxPerson(4)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        Travel travel3 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터3")
                .userNumber(1)
                .viewCount(0)
                .maxPerson(6)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedLocation)
                .build());
        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .personTypes(List.of("2명", "5명 이상"))
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition, user.getUserNumber());

        // then
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getContent().stream().map(c -> c.getTravelNumber())).contains(travel1.getNumber());
        assertThat(result.getContent().stream().map(c -> c.getTravelNumber())).doesNotContain(travel2.getNumber());
        assertThat(result.getContent().stream().map(c -> c.getTravelNumber())).contains(travel3.getNumber());
    }

  /*  @DisplayName("search: 장소 필터링을 통해 국내 또는 해외 여행을 검색할 수 있다")
    @Test
    @DirtiesContext
    public void searchWithLocationFilter() {
        // given
        Location domesticLocation = Location.builder()
                .locationName("Seoul"+System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location internationalLocation = Location.builder()
                .locationName("London"+System.currentTimeMillis())
                .locationType(LocationType.INTERNATIONAL)
                .build();

        Location savedDomesticLocation = locationRepository.save(domesticLocation);
        Location savedInternationalLocation = locationRepository.save(internationalLocation);

        Travel domesticTravel = travelRepository.save(Travel.builder()
                .title("국내 여행")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedDomesticLocation)
                .build());

        Travel internationalTravel = travelRepository.save(Travel.builder()
                .title("해외 여행")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .location(savedInternationalLocation)
                .build());

        // when: "국내" 필터링
        TravelSearchCondition domesticCondition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .locationTypes(List.of("국내"))
                .build();

        Page<TravelSearchDto> domesticResults = travelRepository.search(domesticCondition);

        // then
        assertThat(domesticResults.getTotalElements()).isEqualTo(1);
        assertThat(domesticResults.getContent().get(0).getTitle()).isEqualTo("국내 여행");

        // when: "해외" 필터링
        TravelSearchCondition internationalCondition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .locationTypes(List.of("해외"))
                .build();

        Page<TravelSearchDto> internationalResults = travelRepository.search(internationalCondition);

        // then
        assertThat(internationalResults.getTotalElements()).isEqualTo(1);
        assertThat(internationalResults.getContent().get(0).getTitle()).isEqualTo("해외 여행");

        // when: "국내", "해외" 모두 필터링
        TravelSearchCondition bothCondition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .locationTypes(List.of("국내", "해외"))
                .build();

        Page<TravelSearchDto> bothResults = travelRepository.search(bothCondition);

        // then
        assertThat(bothResults.getTotalElements()).isEqualTo(2);
        assertThat(bothResults.getContent().stream().map(TravelSearchDto::getTitle))
                .containsExactlyInAnyOrder("국내 여행", "해외 여행");
    }*/


//    @DisplayName("findEnrollmentsByUserNumber: 사용자의 신청한 여행 목록 조회")
//    @Test
//    public void findEnrollmentsByUserNumber_ShouldReturnListOfEnrollments() {
//        // given
//        int userNumber = 1;
//
//        // when
//        List<Tuple> results = enrollmentCustomRepository.findEnrollmentsByUserNumber(userNumber);
//
//        // then
//        assertThat(results).isNotNull();
//        assertThat(results.size()).isGreaterThan(0);
//        Tuple tuple = results.get(0);
//        assertThat(tuple.get(0, Long.class)).isNotNull(); // Enrollment number
//        assertThat(tuple.get(1, Integer.class)).isNotNull(); // Travel number
//
//        System.out.println("Enrollment Number: " + tuple.get(0, Long.class));
//        System.out.println("Travel Number: " + tuple.get(1, Integer.class));
//    }
}