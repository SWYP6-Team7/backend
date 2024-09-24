package swyp.swyp6_team7.travel.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import swyp.swyp6_team7.config.DataConfig;
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
import swyp.swyp6_team7.travel.dto.TravelSearchCondition;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;
import swyp.swyp6_team7.travel.dto.response.TravelRecentDto;
import swyp.swyp6_team7.travel.dto.TravelRecommendDto;
import swyp.swyp6_team7.travel.dto.response.TravelSearchDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


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

    @BeforeEach
    void setUp() {
        travelRepository.save(Travel.builder()
                .title("기본 테스트 데이터")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .build());
    }

    @DisplayName("getDetailsByNumber: 여행콘텐츠 식별자로 디테일 정보를 가져온다")
    @Test
    public void getDetailsByNumber() {
        // given
        Users user = userRepository.save(Users.builder()
                .userEmail("test@naver.com")
                .userPw("1234")
                .userName("모잉")
                .userGender(Users.Gender.M)
                .userAgeGroup(Users.AgeGroup.TEEN)
                .userRegDate(LocalDateTime.now())
                .userStatus(Users.MemberStatus.ABLE)
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
                .build());
        travelTagRepository.save(TravelTag.of(travel, tag1));
        travelTagRepository.save(TravelTag.of(travel, tag2));

        // when
        TravelDetailResponse details = travelRepository.getDetailsByNumber(travel.getNumber());

        // then
        System.out.println(details.toString());
        assertThat(details.getTitle()).isEqualTo("추가 테스트 데이터1");
        assertThat(details.getUserNumber()).isEqualTo(user.getUserNumber());
        assertThat(details.getUserName()).isEqualTo("모잉");
        assertThat(details.getTags().size()).isEqualTo(2);
    }

    @DisplayName("findAll: 여행 콘텐츠를 DTO로 만들어 최신순으로 정렬해 반환한다")
    @Test
    public void findAllSortedByCreatedAt() {
        // given
        Travel travel = Travel.builder()
                .title("추가 테스트 데이터")
                .userNumber(1)
                .viewCount(0)
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
                .findAllSortedByCreatedAt(PageRequest.of(0, 5));

        // then
        for (TravelRecentDto result : results) {
            System.out.println(result.toString());
        }
        assertThat(results.getContent().size()).isEqualTo(2);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("추가 테스트 데이터");
        assertThat(results.getContent().get(0).getTags().size()).isEqualTo(3);
    }

    @DisplayName("findAll: 최신순으로 정렬해 반환할 때 데이터가 없을 경우에도 오류가 나지 않는다")
    @Test
    public void findAllSortedByCreatedAtNoData() {
        // given
        travelRepository.deleteAll();

        // when
        Page<TravelRecentDto> results = travelRepository
                .findAllSortedByCreatedAt(PageRequest.of(0, 5));

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
                .build());
        //tags: 한국, 투어, 도시 -> 3개
        travelTagRepository.save(TravelTag.of(travel5, tag1));
        travelTagRepository.save(TravelTag.of(travel5, tag2));
        travelTagRepository.save(TravelTag.of(travel5, tag3));

        // when
        List<TravelRecommendDto> result = travelRepository.findAllByPreferredTags(preferredTags);

        // then
        assertThat(result.size()).isEqualTo(5); //0, 1, 2, 1, 3
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
        Travel travel = Travel.builder()
                .title("추가 테스트 데이터")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .build();
        travelRepository.save(travel);

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .keyword("추가")
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition);

        // then
        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent().size()).isEqualTo(1);
    }

    @DisplayName("search: 제목과 장소에 keyword가 포함된 데이터를 찾을 수 있다")
    @Test
    public void searchWithKeywordThroughTitleAndLocation() {
        // given
        Travel travel = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터")
                .userNumber(1)
                .viewCount(0)
                .location("영국")
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .build());
        Travel travel2 = travelRepository.save(Travel.builder()
                .title("영국 테스트 데이터")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .build());

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .keyword("영국")
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition);

        // then
        assertThat(results.getTotalElements()).isEqualTo(2);
        assertThat(results.getContent().size()).isEqualTo(2);
    }

    @DisplayName("search: 키워드가 주어지지 않을 경우 가능한 모든 콘텐츠를 전달")
    @Test
    public void searchWithoutKeyword() {
        // given
        Travel travel = Travel.builder()
                .title("추가 테스트 데이터")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .build();
        travelRepository.save(travel);

        TravelSearchCondition condition = TravelSearchCondition.builder()
                //.keyword("")
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition);

        // then
        assertThat(results.getTotalElements()).isEqualTo(2);
        assertThat(results.getContent().size()).isEqualTo(2);
    }

    @DisplayName("search: 콘텐츠의 상태가 DELETED, DRAFT일 경우 제외하고 가져온다")
    @Test
    public void searchOnlyActivated() {
        // given
        Travel deletedTravel = Travel.builder()
                .title("추가 테스트 데이터1")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.DELETED)
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
                .build();
        travelRepository.save(draftTravel);

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .keyword("데이터")
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> results = travelRepository.search(condition);

        // then
        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent().size()).isEqualTo(1);
    }

    @DisplayName("search: 페이징을 이용해 콘텐츠를 가져올 수 있다")
    @Test
    public void searchWithPaging() {
        // given
        for (int i = 0; i < 6; i++) {
            travelRepository.save(Travel.builder()
                    .title("추가 테스트 데이터")
                    .userNumber(1)
                    .viewCount(0)
                    .periodType(PeriodType.NONE)
                    .genderType(GenderType.NONE)
                    .createdAt(LocalDateTime.now())
                    .status(TravelStatus.IN_PROGRESS)
                    .build());
        }

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .keyword("추가")
                .pageRequest(PageRequest.of(0, 5))
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition);

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
        for (int i = 0; i < 6; i++) {
            Travel travel = travelRepository.save(Travel.builder()
                    .title("추가 테스트 데이터" + i)
                    .userNumber(1)
                    .viewCount(0)
                    .periodType(PeriodType.NONE)
                    .genderType(GenderType.NONE)
                    .createdAt(LocalDateTime.now())
                    .status(TravelStatus.IN_PROGRESS)
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
        Page<TravelSearchDto> result = travelRepository.search(condition);

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

        Travel travel1 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터1")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
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
        Page<TravelSearchDto> result = travelRepository.search(condition);

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
        Travel travel1 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터1")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.WOMAN_ONLY)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .build());
        Travel travel2 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터2")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.MIXED)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .build());
        Travel travel3 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터3")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.MAN_ONLY)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .build());
        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .genderTypes(new ArrayList<>(List.of(GenderType.WOMAN_ONLY.toString(), GenderType.MIXED.toString())))
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition);

        // then
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getContent().stream().map(c -> c.getTravelNumber())).contains(travel1.getNumber());
        assertThat(result.getContent().stream().map(c -> c.getTravelNumber())).contains(travel2.getNumber());
    }

    @DisplayName("search: 주어지는 기간 타입에 알맞은 콘텐츠를 가져올 수 있다")
    @Test
    public void searchWithPeriodFilter() {
        // given
        Travel travel1 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터1")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.MORE_THAN_MONTH)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .build());
        Travel travel2 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터2")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.THREE_WEEKS)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .build());
        Travel travel3 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터3")
                .userNumber(1)
                .viewCount(0)
                .periodType(PeriodType.TWO_WEEKS)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
                .build());
        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .periodTypes(new ArrayList<>(List.of(
                        PeriodType.MORE_THAN_MONTH.toString(),
                        PeriodType.THREE_WEEKS.toString()))
                )
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition);

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
        Travel travel1 = travelRepository.save(Travel.builder()
                .title("추가 테스트 데이터1")
                .userNumber(1)
                .viewCount(0)
                .maxPerson(1)
                .periodType(PeriodType.NONE)
                .genderType(GenderType.NONE)
                .createdAt(LocalDateTime.now())
                .status(TravelStatus.IN_PROGRESS)
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
                .build());
        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(0, 5))
                .personTypes(List.of("2명", "5명 이상"))
                .build();

        // when
        Page<TravelSearchDto> result = travelRepository.search(condition);

        // then
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getContent().stream().map(c -> c.getTravelNumber())).contains(travel1.getNumber());
        assertThat(result.getContent().stream().map(c -> c.getTravelNumber())).doesNotContain(travel2.getNumber());
        assertThat(result.getContent().stream().map(c -> c.getTravelNumber())).contains(travel3.getNumber());
    }


}