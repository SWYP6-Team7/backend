package swyp.swyp6_team7.travel.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.tag.service.TravelTagService;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class TravelServiceTest {

    @Autowired
    private TravelService travelService;

    @Autowired
    private TravelRepository travelRepository;

    @Mock
    private TravelTagService travelTagService;

    @Mock
    private LocationRepository locationRepository;

    @AfterEach
    void tearDown() {
        travelRepository.deleteAllInBatch();
    }

    @DisplayName("create: 여행 콘텐츠를 만들 수 있다")
    @Test
    @DirtiesContext
    public void createTravelWithUser() {
        // given
        travelRepository.deleteAllInBatch();

        Users user = createUser();
        Location travelLocation = new Location(1L, "서울", LocationType.DOMESTIC);

        LocalDate dueDate = LocalDate.of(2024, 11, 4);
        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("Seoul")
                .title("여행 제목")
                .details("여행 내용")
                .maxPerson(2)
                .genderType(GenderType.MIXED.toString())
                .dueDate(dueDate)
                .periodType(PeriodType.ONE_WEEK.toString())
                .tags(List.of())
                .completionStatus(true)
                .build();

        given(locationRepository.findByLocationName(anyString()))
                .willReturn(Optional.of(travelLocation));
        given(travelTagService.create(any(Travel.class), anyList()))
                .willReturn(List.of());

        // when
        Travel createdTravel = travelService.create(request, user.getUserNumber());

        // then
        assertThat(travelRepository.findAll()).hasSize(1);
        assertThat(createdTravel.getUserNumber()).isEqualTo(1);
        assertThat(createdTravel.getLocationName()).isEqualTo("Seoul");
        assertThat(createdTravel.getTitle()).isEqualTo("여행 제목");
        assertThat(createdTravel.getDetails()).isEqualTo("여행 내용");
        assertThat(createdTravel.getViewCount()).isEqualTo(0);
        assertThat(createdTravel.getMaxPerson()).isEqualTo(2);
        assertThat(createdTravel.getGenderType()).isEqualTo(GenderType.MIXED);
        assertThat(createdTravel.getDueDate()).isEqualTo(dueDate);
        assertThat(createdTravel.getPeriodType()).isEqualTo(PeriodType.ONE_WEEK);
        assertThat(createdTravel.getStatus()).isEqualTo(TravelStatus.IN_PROGRESS);
        assertThat(createdTravel.getEnrollmentsLastViewedAt()).isNull();
        assertThat(createdTravel.getTravelTags()).isEmpty();
        assertThat(createdTravel.getCompanions()).isEmpty();
        assertThat(createdTravel.getDeletedUser()).isNull();
    }

    private Users createUser() {
        return Users.builder()
                .userNumber(1)
                .userEmail("test@naver.com")
                .userPw("1234")
                .userName("사용자명")
                .userGender(Gender.M)
                .userAgeGroup(AgeGroup.TWENTY)
                .userRegDate(LocalDateTime.now())
                .userStatus(UserStatus.ABLE)
                .build();
    }

}