package swyp.swyp6_team7.travel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.bookmark.repository.BookmarkRepository;
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
    public List<TravelListResponseDto> getTravelListByUser(Integer userNumber) {
        // 사용자 번호를 통해 여행 게시글 조회 (최신 등록순으로 정렬)
        List<Travel> travels = travelRepository.findByUserNumber(userNumber).stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt())) // 최신순으로 정렬
                .collect(Collectors.toList());

        // 여행 엔티티를 DTO로 변환하여 반환
        return travels.stream().map(travel -> {

            // 동반자 수 계산
            int currentApplicants = travel.getCompanions().size();

            // 사용자의 이름을 가져오기 위해 userNumber로 사용자 조회
            String username = userRepository.findByUserNumber(travel.getUserNumber())
                    .map(users -> users.getUserName())
                    .orElse("Unknown"); // 해당 사용자를 찾지 못할 경우 기본값

            // 태그 리스트 추출
            List<String> tags = travel.getTravelTags().stream()
                    .map(travelTag -> travelTag.getTag().getName())
                    .collect(Collectors.toList());

            // 북마크 여부 확인
            boolean isBookmarked = bookmarkRepository.existsByUserNumberAndTravelNumber(userNumber, travel.getNumber());

            return new TravelListResponseDto(
                    travel.getNumber(),
                    travel.getTitle(),
                    travel.getUserNumber(),
                    username,
                    tags,
                    currentApplicants,
                    travel.getMaxPerson(),
                    travel.getCreatedAt().toString(),
                    travel.getDueDate().toString(),
                    isBookmarked
            );
        }).collect(Collectors.toList());
    }
}