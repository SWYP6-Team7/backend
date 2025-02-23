package swyp.swyp6_team7.travel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.dto.response.TravelListResponseDto;
import swyp.swyp6_team7.travel.repository.TravelRepository;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.domain.TravelTag;
import swyp.swyp6_team7.bookmark.repository.BookmarkRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TravelListServiceTest {

    @Mock
    private TravelRepository travelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;  // 추가된 부분

    @InjectMocks
    private TravelListService travelListService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("사용자가 작성한 여행 목록 조회 테스트")
    void testGetTravelListByUser() {
        // Given
        Integer userNumber = 1;
        Pageable pageable = PageRequest.of(0, 2);
        Location travelLocation = Location.builder()
                .locationName("Seoul")
                .locationType(LocationType.DOMESTIC)
                .build();

        Travel travel1 = Travel.builder()
                .number(1)
                .userNumber(userNumber)
                .createdAt(LocalDateTime.now().minusDays(2))
                .title("Title 1")
                .maxPerson(5)
                .location(travelLocation)
                .build();

        Travel travel2 = Travel.builder()
                .number(2)
                .userNumber(userNumber)
                .createdAt(LocalDateTime.now().minusDays(6))
                .title("Title 2")
                .maxPerson(10)
                .location(travelLocation)
                .build();

        // 태그 설정
        Tag tag1 = Tag.of("tag1");
        Tag tag2 = Tag.of("tag2");

        TravelTag travelTag1 = new TravelTag(null, travel1, tag1);
        TravelTag travelTag2 = new TravelTag(null, travel2, tag2);

        travel1.getTravelTags().add(travelTag1);
        travel2.getTravelTags().add(travelTag2);

        List<Travel> travels = Arrays.asList(travel1, travel2);

        // 사용자 설정
        Users user = Users.builder().userNumber(userNumber).userName("Username 1").build();

        // DTO 리스트 생성
        List<TravelListResponseDto> dtos = travels.stream().map(travel -> {
            boolean isBookmarked = true; // 예시로 모든 여행을 북마크된 것으로 설정
            List<String> tags = travel.getTravelTags().stream()
                    .map(travelTag -> travelTag.getTag().getName())
                    .collect(Collectors.toList());
            return new TravelListResponseDto(
                    travel.getNumber(),
                    travel.getTitle(),
                    travel.getLocationName(),
                    travel.getUserNumber(),
                    user.getUserName(),
                    tags,
                    travel.getCompanions().size(),
                    travel.getMaxPerson(),
                    travel.getCreatedAt(),
                    isBookmarked
            );
        }).collect(Collectors.toList());

        Page<TravelListResponseDto> dtoPage = new PageImpl<>(dtos, pageable, dtos.size());


        // When
        when(travelRepository.findByUserNumber(userNumber)).thenReturn(travels);
        when(userRepository.findByUserNumber(userNumber)).thenReturn(Optional.of(user));
        when(bookmarkRepository.existsByUserNumberAndTravelNumber(eq(userNumber), anyInt())).thenReturn(true);

        // Then
        Page<TravelListResponseDto> result = travelListService.getTravelListByUser(userNumber, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        TravelListResponseDto travel1Dto = result.getContent().get(0);
        assertEquals("Title 1", travel1Dto.getTitle());
        assertEquals("Username 1", travel1Dto.getUserName());
        assertEquals(List.of("tag1"), travel1Dto.getTags());
        assertEquals(5, travel1Dto.getMaxPerson());
        assertEquals(true, travel1Dto.isBookmarked());

        TravelListResponseDto travel2Dto = result.getContent().get(1);
        assertEquals("Title 2", travel2Dto.getTitle());
        assertEquals("Username 1", travel2Dto.getUserName());
        assertEquals(List.of("tag2"), travel2Dto.getTags());
        assertEquals(10, travel2Dto.getMaxPerson());
        assertEquals(true, travel2Dto.isBookmarked());

        verify(travelRepository, times(1)).findByUserNumber(userNumber);
        verify(bookmarkRepository, times(2)).existsByUserNumberAndTravelNumber(eq(userNumber), anyInt());
    }
}
