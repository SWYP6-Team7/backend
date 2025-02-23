package swyp.swyp6_team7.travel.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.travel.domain.Travel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@AllArgsConstructor
public class TravelListResponseDto {
    private int travelNumber;           // 여행 번호
    private String title;               // 여행 제목
    private String location;            // 여행지
    private int userNumber;             // 사용자 번호
    private String userName;            // 사용자 이름
    private List<String> tags;          // 태그 리스트
    private int nowPerson;              // 현재 참가 인원 수
    private int maxPerson;              // 최대 참가 인원 수
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    private boolean isBookmarked;       // 북마크 여부


    public static TravelListResponseDto fromEntity(Travel travel, Users host, int currentApplicants, boolean isBookmarked) {
        return new TravelListResponseDto(
                travel.getNumber(),
                travel.getTitle(),
                travel.getLocationName(),
                host.getUserNumber(),
                host.getUserName(),
                travel.getTravelTags().stream().map(tag -> tag.getTag().getName()).collect(Collectors.toList()),
                currentApplicants,
                travel.getMaxPerson(),
                travel.getCreatedAt(),
                isBookmarked
        );
    }
}
