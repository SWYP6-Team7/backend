package swyp.swyp6_team7.travel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
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
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
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

    private final TravelRepository travelRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ImageRepository imageRepository;
    private final TravelTagService travelTagService;
    private final MemberService memberService;
    private final LocationRepository locationRepository;
    private final CommentRepository commentRepository;
    private final CommentService commentService;

    @Transactional
    public Travel create(TravelCreateRequest request, String email) {

        Users user = memberService.findByEmail(email);
        // Location 정보가 없으면 새로운 Location 추가 (locationType은 UNKNOWN으로 설정)
        Location location = locationRepository.findByLocationName(request.getLocationName())
                .orElseGet(() -> {
                    Location newLocation = Location.builder()
                            .locationName(request.getLocationName())
                            .locationType(LocationType.UNKNOWN) // UNKNOWN으로 설정
                            .build();
                    return locationRepository.save(newLocation);
                });

        Travel savedTravel = travelRepository.save(request.toTravelEntity(user.getUserNumber(), location));
        List<String> tags = travelTagService.create(savedTravel, request.getTags()).stream()
                .map(tag -> tag.getName())
                .toList();

        return savedTravel;
    }

    public TravelDetailResponse getDetailsByNumber(int travelNumber) {
        Travel travel = travelRepository.findByNumber(travelNumber)
                .orElseThrow(() -> new IllegalArgumentException("travel not found: " + travelNumber));

        if (travel.getStatus() == TravelStatus.DRAFT) {
            authorizeTravelOwner(travel);
        } else if (travel.getStatus() == TravelStatus.DELETED) {
            throw new IllegalArgumentException("Deleted Travel.");
        }

        Integer requestUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        TravelDetailDto travelDetail = travelRepository.getDetailsByNumber(travelNumber, requestUserNumber);

        //주최자 프로필 이미지(만약 못찾을 경우 default 이미지 url)
        String hostProfileImageUrl = imageRepository.findUrlByRelatedUserNumber(travelDetail.getHostNumber())
                .orElse("https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png");

        //enrollment 개수
        int enrollmentCount = enrollmentRepository.countByTravelNumber(travelNumber);

        //bookmark 개수
        int bookmarkCount = bookmarkRepository.countByTravelNumber(travelNumber);

        TravelDetailResponse detailResponse = new TravelDetailResponse(travelDetail, hostProfileImageUrl, enrollmentCount, bookmarkCount);

        //로그인 요청자 주최 여부, 신청 확인
        if (travelDetail.getHostNumber() == requestUserNumber) {
            detailResponse.setHostUserCheckTrue();
        } else {
            Enrollment enrollmented = enrollmentRepository
                    .findOneByUserNumberAndTravelNumber(requestUserNumber, travelNumber);
            //log.info("enrollmented = " + enrollmented);
            detailResponse.setEnrollmentNumber(enrollmented);
        }

        return detailResponse;
    }

    @Transactional
    public void addViewCount(int travelNumber) {
        travelRepository.updateViewCountPlusOneByTravelNumber(travelNumber);
    }

    @Transactional
    public void update(int travelNumber, TravelUpdateRequest travelUpdate) {
        Travel travel = travelRepository.findByNumber(travelNumber)
                .orElseThrow(() -> new IllegalArgumentException("travel not found: " + travelNumber));

        authorizeTravelOwner(travel);

        // Location 정보가 없으면 새로운 Location 추가 (locationType은 UNKNOWN으로 설정)
        Location location = locationRepository.findByLocationName(travelUpdate.getLocationName())
                .orElseGet(() -> {
                    Location newLocation = Location.builder()
                            .locationName(travelUpdate.getLocationName())
                            .locationType(LocationType.UNKNOWN) // UNKNOWN으로 설정
                            .build();
                    return locationRepository.save(newLocation);
                });

        Travel updatedTravel = travel.update(travelUpdate, location);
        List<String> updatedTags = travelTagService.update(updatedTravel, travelUpdate.getTags());
    }

    @Transactional
    public void delete(int travelNumber) {
        Travel travel = travelRepository.findByNumber(travelNumber)
                .orElseThrow(() -> new IllegalArgumentException("travel not found: " + travelNumber));

        authorizeTravelOwner(travel);

        //댓글 삭제
        List<Comment> comments = commentRepository.findByRelatedTypeAndRelatedNumber("travel", travel.getNumber());
        for (Comment comment : comments) {
            commentService.delete(comment.getCommentNumber(), travel.getUserNumber());
        }

        travel.delete();


    }

    private void authorizeTravelOwner(Travel travel) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        int userNumber = memberService.findByEmail(userName).getUserNumber();
        if (travel.getUserNumber() != userNumber) {
            throw new IllegalArgumentException("Forbidden Travel");
        }
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
