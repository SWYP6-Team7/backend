package swyp.swyp6_team7.travel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.bookmark.repository.BookmarkRepository;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.dto.response.TravelListResponseDto;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.repository.TravelRepository;
import swyp.swyp6_team7.travel.domain.TravelStatus;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TravelListService {

    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

    @Transactional(readOnly = true)
    public Page<TravelListResponseDto> getTravelListByUser(Integer userNumber, Pageable pageable) {
        // 사용자 번호를 통해 여행 게시글 조회 (최신 등록순으로 정렬)
        List<Travel> travels = travelRepository.findByUserNumber(userNumber).stream()
                .filter(travel -> travel.getStatus() != TravelStatus.DELETED) // 삭제된 여행 제외
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt())) // 최신순으로 정렬
                .collect(Collectors.toList());

        // 여행 엔티티를 DTO로 변환하여 반환
        List<TravelListResponseDto> dtos = travels.stream()
                .map(travel -> toTravelListResponseDto(travel, userNumber))
                .collect(Collectors.toList());

        return toPage(dtos, pageable);
    }

    // Travel 엔티티를 TravelListResponseDto로 변환하는 메서드
    private TravelListResponseDto toTravelListResponseDto(Travel travel, Integer userNumber) {

            // 동반자 수 계산
            int currentApplicants = travel.getCompanions().size();

            // 사용자의 이름을 가져오기 위해 userNumber로 사용자 조회
            Users host = userRepository.findByUserNumber(travel.getUserNumber())
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보를 찾을 수 없습니다."));


            // 태그 리스트 추출
            List<String> tags = travel.getTravelTags().stream()
                    .map(travelTag -> travelTag.getTag().getName())
                    .collect(Collectors.toList());

            // 북마크 여부 확인
            boolean isBookmarked = bookmarkRepository.existsByUserNumberAndTravelNumber(userNumber, travel.getNumber());


            return new TravelListResponseDto(
                    travel.getNumber(),
                    travel.getTitle(),
                    travel.getLocationName(),
                    host.getUserNumber(),
                    host.getUserName(),
                    tags,
                    currentApplicants,
                    travel.getMaxPerson(),
                    travel.getCreatedAt(),
                    isBookmarked
            );
        }

    // Page 객체를 생성하는 메서드
    private Page<TravelListResponseDto> toPage(List<TravelListResponseDto> dtos, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());

        if (start > end || start > dtos.size()) {
            return new PageImpl<>(List.of(), pageable, dtos.size());
        }
        return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
    }

    // 사용자가 만든 여행(삭제되지 않은)의 총 개수를 반환하는 메서드
    @Transactional(readOnly = true)
    public int countCreatedTravelsByUser(Integer userNumber) {
        // 여행 리스트 조회 시, 삭제되지 않은 여행만 카운트
        List<Travel> travels = travelRepository.findByUserNumber(userNumber).stream()
                .filter(travel -> travel.getStatus() != TravelStatus.DELETED)
                .collect(Collectors.toList());
        return travels.size();
    }
}
