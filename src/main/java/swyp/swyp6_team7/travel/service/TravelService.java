package swyp.swyp6_team7.travel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.bookmark.repository.BookmarkRepository;
import swyp.swyp6_team7.comment.domain.Comment;
import swyp.swyp6_team7.comment.repository.CommentRepository;
import swyp.swyp6_team7.comment.service.CommentService;
import swyp.swyp6_team7.enrollment.domain.Enrollment;
import swyp.swyp6_team7.enrollment.repository.EnrollmentRepository;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.member.util.MemberAuthorizeUtil;
import swyp.swyp6_team7.tag.service.TravelTagService;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.TravelDetailDto;
import swyp.swyp6_team7.travel.dto.TravelSearchCondition;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.dto.request.TravelUpdateRequest;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;
import swyp.swyp6_team7.travel.dto.response.TravelSearchDto;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TravelService {

    private final static String DEFAULT_PROFILE_IMAGE_URL = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png";

    private final TravelTagService travelTagService;
    private final CommentService commentService;
    private final TravelRepository travelRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ImageRepository imageRepository;
    private final LocationRepository locationRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public Travel create(TravelCreateRequest request, int loginUserNumber) {

        // Location 정보가 없으면 새로운 Location 추가 (locationType은 UNKNOWN으로 설정)
        Location location = getLocation(request.getLocationName());

        Travel savedTravel = travelRepository.save(request.toTravelEntity(loginUserNumber, location));
        List<String> tags = travelTagService.create(savedTravel, request.getTags()).stream()
                .map(tag -> tag.getName())
                .toList();

        return savedTravel;
    }

    private Location getLocation(String locationName) {
        Location location = locationRepository.findByLocationName(locationName)
                .orElseGet(() -> {
                    Location newLocation = Location.builder()
                            .locationName(locationName)
                            .locationType(LocationType.UNKNOWN) // UNKNOWN으로 설정
                            .build();
                    log.info("Location 생성 - locationName: {}", newLocation.getLocationName());
                    return locationRepository.save(newLocation);
                });
        return location;
    }

    public TravelDetailResponse getDetailsByNumber(int travelNumber, int requestUserNumber) {
        Travel travel = travelRepository.findByNumber(travelNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행을 찾을 수 없습니다. - travelNumber: " + travelNumber));

        if (travel.getStatus() == TravelStatus.DRAFT) {
            if (travel.getUserNumber() != requestUserNumber) {
                log.warn("DRAFT 상태의 여행 조회 권한이 없습니다. - travelNumber: {}, requestUser: {}", travelNumber, requestUserNumber);
                throw new IllegalArgumentException("DRAFT 상태의 여행 조회는 작성자만 가능합니다.");
            }
        } else if (travel.getStatus() == TravelStatus.DELETED) {
            log.warn("Deleted 상태의 여행 콘텐츠는 상세 조회할 수 없습니다 - travelNumber: {}", travelNumber);
            throw new IllegalArgumentException("Deleted 상태의 여행 콘텐츠입니다.");
        }

        TravelDetailDto travelDetail = travelRepository.getDetailsByNumber(travelNumber, requestUserNumber);

        //주최자 프로필 이미지(만약 못찾을 경우 default 이미지 url)
        String hostProfileImageUrl = imageRepository.findUrlByRelatedUserNumber(travelDetail.getHostNumber())
                .orElse(DEFAULT_PROFILE_IMAGE_URL);

        //enrollment 개수
        int enrollmentCount = enrollmentRepository.countByTravelNumber(travelNumber);

        //bookmark 개수
        int bookmarkCount = bookmarkRepository.countByTravelNumber(travelNumber);

        // TravelDetailResponse 생성
        TravelDetailResponse detailResponse = new TravelDetailResponse(travelDetail, hostProfileImageUrl, enrollmentCount, bookmarkCount);

        //로그인 요청자 주최 여부, 신청 확인
        if (travelDetail.getHostNumber() == requestUserNumber) {
            detailResponse.setHostUserCheckTrue();
        } else {
            Enrollment enrollmented = enrollmentRepository
                    .findOneByUserNumberAndTravelNumber(requestUserNumber, travelNumber);
            detailResponse.setEnrollmentNumber(enrollmented);
        }

        return detailResponse;
    }

    @Transactional
    public void addViewCount(int travelNumber) {
        travelRepository.updateViewCountPlusOneByTravelNumber(travelNumber);
    }

    @Transactional
    public Travel update(int travelNumber, TravelUpdateRequest request, int requestUserNumber) {
        Travel travel = travelRepository.findByNumber(travelNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행을 찾을 수 없습니다. - travelNumber: " + travelNumber));

        if (travel.getUserNumber() != requestUserNumber) {
            log.warn("여행 수정 권한이 없습니다. - travelNumber: {}, requestUser: {}", travelNumber, requestUserNumber);
            throw new IllegalArgumentException("여행 수정 권한이 없습니다.");
        }

        // Location 정보가 없으면 새로운 Location 추가 (locationType은 UNKNOWN으로 설정)
        Location location = getLocation(request.getLocationName());

        Travel updatedTravel = travel.update(request, location);
        //List<String> updatedTags = travelTagService.update(updatedTravel, request.getTags());
        travelTagService.update(updatedTravel, request.getTags());
        return updatedTravel;
    }

    @Transactional
    public void delete(int travelNumber, int requestUserNumber) {
        Travel travel = travelRepository.findByNumber(travelNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행을 찾을 수 없습니다. - travelNumber: " + travelNumber));

        if (travel.getUserNumber() != requestUserNumber) {
            log.warn("여행 삭제 권한이 없습니다. - travelNumber: {}, requestUser: {}", travelNumber, requestUserNumber);
            throw new IllegalArgumentException("여행 삭제 권한이 없습니다.");
        }

        //댓글 삭제
        List<Comment> comments = commentRepository.findByRelatedTypeAndRelatedNumber("travel", travel.getNumber());
        for (Comment comment : comments) {
            commentService.delete(comment.getCommentNumber(), travel.getUserNumber());
        }

        travel.delete();
        travelRepository.save(travel);
    }


    public Page<TravelSearchDto> search(TravelSearchCondition condition) {
        Integer requestUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        Page<TravelSearchDto> result = travelRepository.search(condition, requestUserNumber);
        return result;
    }


    public LocalDateTime getEnrollmentsLastViewedAt(int travelNumber) {
        return travelRepository.getEnrollmentsLastViewedAtByNumber(travelNumber);
    }

    @Transactional
    public void updateEnrollmentLastViewedAt(int travelNumber, LocalDateTime lastViewedAt) {
        travelRepository.updateEnrollmentsLastViewedAtByNumber(travelNumber, lastViewedAt);
    }

    public List<Travel> getTravelsByDeletedUser(Integer deletedUserNumber) {
        return travelRepository.findByDeletedUserNumber(deletedUserNumber);
    }
}
