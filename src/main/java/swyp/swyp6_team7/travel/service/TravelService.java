package swyp.swyp6_team7.travel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.bookmark.repository.BookmarkRepository;
import swyp.swyp6_team7.comment.repository.CommentRepository;
import swyp.swyp6_team7.enrollment.repository.EnrollmentRepository;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.plan.service.PlanService;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.domain.TravelTag;
import swyp.swyp6_team7.tag.service.TagService;
import swyp.swyp6_team7.tag.service.TravelTagService;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.TravelDetailDto;
import swyp.swyp6_team7.travel.dto.TravelDetailLoginMemberRelatedDto;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.dto.request.TravelUpdateRequest;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TravelService {

    public static final int TRAVEL_TAG_MAX_COUNT = 5;
    public static final int TRAVEL_MAX_RANGE = 90;

    // TODO: 링크 수정
    private final static String DEFAULT_PROFILE_IMAGE_URL = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png";

    private final TravelTagService travelTagService;
    private final TagService tagService;
    private final PlanService planService;
    private final TravelRepository travelRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ImageRepository imageRepository;
    private final LocationRepository locationRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public Travel create(TravelCreateRequest request, int loginUserNumber) {
        validateTravelDaysRange(request.getStartDate(), request.getEndDate());

        // 일정 개수 검증
        long travelDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        if (travelDays < request.getPlans().size()) {
            throw new MoingApplicationException("여행 일정 개수는 여행 기간을 초과할 수 없습니다.");
        }

        try {
            Location location = getLocation(request.getLocationName());
            List<Tag> tags = getTags(request.getTags());

            Travel createdTravel = travelRepository.save(
                    Travel.create(loginUserNumber, location,
                            request.getStartDate(), request.getEndDate(), request.getTitle(), request.getDetails(),
                            request.getMaxPerson(), request.getGenderType(), request.getPeriodType(), tags)
            );

            planService.createPlans(createdTravel.getNumber(), request.getPlans());    // 여행 일정 생성

            log.info("여행 생성 완료: travelNumber={}", createdTravel.getNumber());
            return createdTravel;

        } catch (Exception e) {
            log.warn("여행 생성 중 오류 발생: {}", e.getMessage());
            throw new MoingApplicationException("여행 생성 과정에서 오류가 발생했습니다.");
        }
    }

    private List<Tag> getTags(List<String> tagNames) {
        return tagNames.stream()
                .distinct().limit(TRAVEL_TAG_MAX_COUNT)
                .map(name -> tagService.findByName(name))
                .toList();
    }

    private Location getLocation(String locationName) {
        Location location = locationRepository.findByLocationName(locationName)
                .orElseGet(() -> {
                    // Location 정보가 없으면 새로운 Location 추가 (locationType은 UNKNOWN으로 설정)
                    Location newLocation = Location.builder()
                            .locationName(locationName)
                            .locationType(LocationType.UNKNOWN) // UNKNOWN으로 설정
                            .build();
                    log.info("Location 생성: locationName={}", newLocation.getLocationName());
                    return locationRepository.save(newLocation);
                });
        return location;
    }

    private void validateTravelDaysRange(LocalDate startDate, LocalDate endDate) {
        long travelDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // 여행 최대 기간을 초과하면 예외 발생
        if (travelDays > TRAVEL_MAX_RANGE) {
            throw new MoingApplicationException("여행 기간은 90일을 초과할 수 없습니다.");
        }
    }

    public TravelDetailResponse getDetailsByNumber(int travelNumber) {

        boolean travelExistence = travelRepository.existsTravelByNumber(travelNumber);
        if (!travelExistence) {
            log.warn("존재하지 않는 여행 조회 요청: travelNumber={}", travelNumber);
            throw new MoingApplicationException("해당하는 여행을 찾을 수 없습니다. travelNumber=" + travelNumber);
        }

        // 여행 상세 정보 조회
        TravelDetailDto travelDetail = travelRepository.getDetailsByNumber(travelNumber);

        // 여행 상태가 삭제인 경우 예외 처리
        if (travelDetail.getTravelStatus() == TravelStatus.DELETED) {
            log.warn("DELETED 상태 여행 상세 조회 요청: travelNumber={}", travelNumber);
            throw new MoingApplicationException("삭제된 여행 콘텐츠입니다.");
        }

        // 주최자 프로필 이미지 (만약 못찾을 경우 default 프로필 이미지로 설정)
        String hostProfileImageUrl = imageRepository.findUrlByRelatedUserNumber(travelDetail.getHostNumber())
                .orElse(DEFAULT_PROFILE_IMAGE_URL);

        // enrollment 개수
        int enrollmentCount = enrollmentRepository.countByTravelNumber(travelNumber);

        // bookmark 개수
        int bookmarkCount = bookmarkRepository.countByTravelNumber(travelNumber);

        return new TravelDetailResponse(travelDetail, hostProfileImageUrl, enrollmentCount, bookmarkCount);
    }

    public TravelDetailLoginMemberRelatedDto getTravelDetailMemberRelatedInfo(int requestUserNumber, int travelNumber, int hostNumber, String postStatus) {

        // DRAFT 여부 체크
        if (postStatus.equals(TravelStatus.DRAFT.getName())) {
            if (hostNumber != requestUserNumber) {
                log.warn("DRAFT 상태 여행 조회 권한이 없습니다:  travelNumber={}, requestUser={}", travelNumber, requestUserNumber);
                throw new IllegalArgumentException("DRAFT 상태의 여행 조회는 작성자만 가능합니다.");
            }
        }

        TravelDetailLoginMemberRelatedDto loginMemberRelatedInfo = new TravelDetailLoginMemberRelatedDto();

        // 주최자 여부, 신청 여부
        if (hostNumber == requestUserNumber) {
            loginMemberRelatedInfo.setHostUserCheckTrue();
        } else {
            // PENDING 상태의 최신 Enrollment의 식별자를 가져온다 (없으면 NULL)
            Long enrollmentNumber = enrollmentRepository.findPendingEnrollmentByTravelNumberAndUserNumber(travelNumber, requestUserNumber);
            loginMemberRelatedInfo.setEnrollmentNumber(enrollmentNumber);
        }

        // 북마크 여부
        boolean isBookmarked = bookmarkRepository.existsByUserNumberAndTravelNumber(requestUserNumber, travelNumber);
        if (isBookmarked) {
            loginMemberRelatedInfo.setBookmarkedTrue();
        }

        return loginMemberRelatedInfo;
    }

    @Transactional
    public Travel update(int travelNumber, TravelUpdateRequest request, int requestUserNumber) {
        validateTravelDaysRange(request.getStartDate(), request.getEndDate());  // 여행 기간 검증

        Travel travel = travelRepository.findByNumber(travelNumber)
                .orElseThrow(() -> new MoingApplicationException("해당하는 여행을 찾을 수 없습니다. travelNumber=" + travelNumber));

        if (travel.getUserNumber() != requestUserNumber) {
            log.warn("여행 수정 권한 없음: travelNumber={}, requestUser={}", travelNumber, requestUserNumber);
            throw new MoingApplicationException("여행 수정 권한이 없습니다.");
        }

        // 여행 일정 개수 검증
        long travelDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        int changedPlanSize = planService.getTravelPlanCount(travelNumber) + request.getPlanChanges().getPlanSizeChangeValue();
        //log.info("여행 기간={}, 기대 일정 개수={}", travelDays, changedPlanSize);
        if (changedPlanSize > travelDays || changedPlanSize > TRAVEL_MAX_RANGE || changedPlanSize < 0) {
            throw new MoingApplicationException("잘못된 여행 일정 개수입니다.");
        }

        Location location = getLocation(request.getLocationName());
        List<String> requestTagsName = request.getTags().stream()
                .distinct().limit(TRAVEL_TAG_MAX_COUNT)
                .toList();
        List<TravelTag> travelTags = travelTagService.update(travel, requestTagsName);

        Travel updatedTravel = travel.update(location, request.getStartDate(), request.getEndDate(),
                request.getTitle(), request.getDetails(), request.getMaxPerson(), request.getGenderType(),
                request.getPeriodType(), travelTags
        );

        try {
            planService.updatePlans(updatedTravel.getNumber(), request.getPlanChanges()); // 여행 일정 수정
            log.info("여행 일정 수정 완료: travelNumber={}", travelNumber);
        } catch (Exception e) {
            log.warn("여행 일정 수정 중 오류 발생: {}", e.getMessage());
            throw new MoingApplicationException("여행 일정 수정 과정에서 오류가 발생했습니다.");
        }

        log.info("여행 수정 완료: travelNumber={}", updatedTravel.getNumber());
        return updatedTravel;
    }

    @Transactional
    public void delete(int travelNumber, int requestUserNumber) {
        Travel travel = travelRepository.findByNumber(travelNumber)
                .orElseThrow(() -> new MoingApplicationException("해당하는 여행을 찾을 수 없습니다. travelNumber=" + travelNumber));

        if (travel.getUserNumber() != requestUserNumber) {
            log.warn("여행 삭제 권한 없음: travelNumber={}, requestUser={}", travelNumber, requestUserNumber);
            throw new MoingApplicationException("여행 삭제 권한이 없습니다.");
        }
        if (travel.getStatus() == TravelStatus.DELETED) {
            throw new MoingApplicationException("이미 삭제된 여행입니다.");
        }

        try {
            commentRepository.deleteCommentsByRelatedTypeAndRelatedNumber("travel", travel.getNumber()); // 댓글 전체 삭제
            planService.deleteAllPlansAndRelatedSpots(travel.getNumber()); // 일정 전체 삭제
            travel.delete(); // 여행 상태 DELETED 설정
            log.info("여행 삭제 완료: travelNumber={}", travelNumber);
        } catch (Exception e) {
            log.warn("여행 삭제 중 오류 발생: {}", e.getMessage());
            throw new MoingApplicationException("여행 삭제 과정에서 오류가 발생했습니다.");
        }

        log.info("여행 삭제 완료: travelNumber={}", travelNumber);
    }

    public LocalDateTime getEnrollmentsLastViewedAt(int travelNumber) {
        if (!travelRepository.existsTravelByNumber(travelNumber)) {
            log.warn("존재하지 않는 여행 콘텐츠입니다: travelNumber={}", travelNumber);
            throw new IllegalArgumentException("존재하지 않는 여행 콘텐츠입니다.");
        }

        return travelRepository.getEnrollmentsLastViewedAtByNumber(travelNumber);
    }

    @Transactional
    public void updateEnrollmentLastViewedAt(int travelNumber, LocalDateTime lastViewedAt) {
        if (!travelRepository.existsTravelByNumber(travelNumber)) {
            log.warn("존재하지 않는 여행 콘텐츠입니다: travelNumber={}", travelNumber);
            throw new IllegalArgumentException("존재하지 않는 여행 콘텐츠입니다.");
        }

        travelRepository.updateEnrollmentsLastViewedAtByNumber(travelNumber, lastViewedAt);
    }

    public List<Travel> getTravelsByDeletedUser(Integer deletedUserNumber) {
        return travelRepository.findByDeletedUserNumber(deletedUserNumber);
    }
}
