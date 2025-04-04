package swyp.swyp6_team7.travel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import swyp.swyp6_team7.bookmark.repository.BookmarkRepository;
import swyp.swyp6_team7.companion.domain.Companion;
import swyp.swyp6_team7.companion.repository.CompanionRepository;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.response.TravelListResponseDto;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class TravelAppliedServiceTest {

    @Mock
    private TravelRepository travelRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private CompanionRepository companionRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private TravelAppliedService travelAppliedService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("사용자가 신청한 여행 목록 조회")
    void testGetAppliedTripsByUser() {
        // Given
        Integer userNumber = 1;
        Pageable pageable = PageRequest.of(0, 2);

        Travel travel1 = Travel.builder()
                .number(1)
                .userNumber(2)
                .locationName("Location 1")
                .title("Title 1")
                .details("Details 1")
                .viewCount(100)
                .maxPerson(5)
                .genderType(GenderType.MIXED)
                .periodType(PeriodType.THREE_WEEKS)
                .status(TravelStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .build();

        Travel travel2 = Travel.builder()
                .number(2)
                .userNumber(2)
                .locationName("Location 2")
                .title("Title 2")
                .details("Details 2")
                .viewCount(150)
                .maxPerson(10)
                .genderType(GenderType.MAN_ONLY)
                .periodType(PeriodType.THREE_WEEKS)
                .status(TravelStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .build();

        Users host = new Users(); // 사용자 객체 초기화
        host.setUserNumber(2);


        Companion companion1 = Companion.builder()
                .travel(travel1)
                .build();

        Companion companion2 = Companion.builder()
                .travel(travel2)
                .build();

        List<Companion> companions = Arrays.asList(companion1, companion2);

        // 목 객체 행동 정의
        when(companionRepository.findByUserNumber(userNumber)).thenReturn(companions);
        when(userRepository.findByUserNumber(anyInt())).thenReturn(Optional.of(host));
        when(bookmarkRepository.existsByUserNumberAndTravelNumber(anyInt(), anyInt())).thenReturn(false);

        // When
        Page<TravelListResponseDto> result = travelAppliedService.getAppliedTripsByUser(userNumber, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements()); // 총 2개의 여행이 반환되는지 확인
        verify(companionRepository, times(1)).findByUserNumber(userNumber);
        verify(userRepository, times(2)).findByUserNumber(anyInt());
        verify(bookmarkRepository, times(2)).existsByUserNumberAndTravelNumber(anyInt(), anyInt());
    }



    @Test
    @DisplayName("사용자가 특정 여행에 대한 참가 취소")
    public void cancelApplication_ShouldRemoveCompanion() {
        // given
        Integer userNumber = 1;
        int travelNumber = 1;
        Location travelLocation = Location.builder()
                .locationName("Seoul")
                .locationType(LocationType.DOMESTIC)
                .build();
        Travel travel = Travel.builder()
                .number(travelNumber)
                .location(travelLocation)
                .build();
        Companion companion = Companion.builder()
                .travel(travel)
                .userNumber(userNumber)
                .build();

        when(travelRepository.findById(travelNumber)).thenReturn(Optional.of(travel));
        when(companionRepository.findByTravelAndUserNumber(travel, userNumber)).thenReturn(Optional.of(companion));

        // when
        travelAppliedService.cancelApplication(userNumber, travelNumber);

        // then
        verify(companionRepository, times(1)).deleteByTravelAndUserNumber(travel, userNumber);
    }
}