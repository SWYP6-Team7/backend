package swyp.swyp6_team7.enrollment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.companion.domain.Companion;
import swyp.swyp6_team7.companion.repository.CompanionRepository;
import swyp.swyp6_team7.enrollment.domain.Enrollment;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.enrollment.dto.EnrollmentCreateRequest;
import swyp.swyp6_team7.enrollment.dto.EnrollmentResponse;
import swyp.swyp6_team7.enrollment.repository.EnrollmentRepository;
import swyp.swyp6_team7.notification.service.NotificationService;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.dto.response.TravelEnrollmentsResponse;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final TravelRepository travelRepository;
    private final CompanionRepository companionRepository;

    private final NotificationService notificationService;


    @Transactional
    public void create(EnrollmentCreateRequest request, int requestUserNumber, LocalDate nowDate) {

        Travel targetTravel = travelRepository.findByNumber(request.getTravelNumber())
                .orElseThrow(() -> {
                    log.warn("Enrollment create - 존재하지 않는 여행 콘텐츠입니다. travelNumber: {}", request.getTravelNumber());
                    return new IllegalArgumentException("존재하지 않는 여행 콘텐츠입니다.");
                });

        if (!targetTravel.availableForEnroll(nowDate)) {
            log.warn("Enrollment create - 참가 신청 할 수 없는 상태의 여행입니다. travelNumber: {}", targetTravel.getNumber());
            throw new IllegalArgumentException("참가 신청 할 수 없는 상태의 여행입니다.");
        }
        Enrollment created = Enrollment.create(requestUserNumber, request.getTravelNumber(), request.getMessage());
        enrollmentRepository.save(created);

        //알림
        notificationService.createEnrollNotification(targetTravel, requestUserNumber);
    }

    @Transactional
    public void delete(long enrollmentNumber, int requestUserNumber) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentNumber)
                .orElseThrow(() -> {
                    log.warn("Enrollment delete - 존재하지 않는 신청입니다. enrollNumber: {}", enrollmentNumber);
                    return new IllegalArgumentException("존재하지 않는 신청입니다.");
                });

        if (enrollment.getUserNumber() != requestUserNumber) {
            log.warn("Enrollment delete - 여행 참가 신청 취소 권한이 없습니다. enrollNumber: {}, requestUser:{}", enrollmentNumber, requestUserNumber);
            throw new IllegalArgumentException("여행 참가 신청 취소 권한이 없습니다.");
        }

        enrollmentRepository.delete(enrollment);
    }

    public TravelEnrollmentsResponse findEnrollmentsByTravelNumber(int travelNumber, int requestUserNumber) {
        Travel targetTravel = travelRepository.findByNumber(travelNumber)
                .orElseThrow(() -> {
                    log.warn("Enrollments 조회 - 존재하지 않는 여행 콘텐츠입니다. travelNumber: {}", travelNumber);
                    return new IllegalArgumentException("존재하지 않는 여행 콘텐츠입니다.");
                });

        if (!targetTravel.isTravelHostUser(requestUserNumber)) {
            log.warn("Enrollments 조회 - 여행 참가 신청 조회 권한이 없습니다. travelNumber: {}, requestUser:{}", travelNumber, requestUserNumber);
            throw new IllegalArgumentException("여행 참가 신청 조회 권한이 없습니다.");
        }

        List<EnrollmentResponse> enrollments = enrollmentRepository.findEnrollmentsByTravelNumber(travelNumber);
        return TravelEnrollmentsResponse.from(enrollments);
    }

    public long getPendingEnrollmentsCountByTravelNumber(int travelNumber) {
        if (!travelRepository.existsTravelByNumber(travelNumber)) {
            log.warn("Pending Enroll Count - 존재하지 않는 여행 콘텐츠입니다. travelNumber: {}", travelNumber);
            throw new IllegalArgumentException("존재하지 않는 여행 콘텐츠입니다.");
        }

        return enrollmentRepository.countByTravelNumberAndStatus(travelNumber, EnrollmentStatus.PENDING);
    }

    @Transactional
    public void accept(long enrollmentNumber, int requestUserNumber) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentNumber)
                .orElseThrow(() -> {
                    log.warn("Enrollment accept - 존재하지 않는 신청입니다. enrollNumber: {}", enrollmentNumber);
                    return new IllegalArgumentException("존재하지 않는 신청입니다.");
                });

        Travel targetTravel = travelRepository.findByNumber(enrollment.getTravelNumber())
                .orElseThrow(() -> {
                    log.warn("Enrollment accept - 존재하지 않는 여행 콘텐츠입니다. travelNumber: {}", enrollment.getTravelNumber());
                    return new IllegalArgumentException("존재하지 않는 여행 콘텐츠입니다.");
                });

        // 여행 주최자 권한 확인
        if (!targetTravel.isTravelHostUser(requestUserNumber)) {
            log.warn("Enrollment accept - 여행 참가 신청 수락 권한이 없습니다. enrollNumber: {}, requestUser:{}", enrollmentNumber, requestUserNumber);
            throw new IllegalArgumentException("여행 참가 신청 수락 권한이 없습니다.");
        }

        // 여행 참가 모집 인원 확인
        if (!targetTravel.availableForAddCompanion()) {
            log.warn("Enrollment accept - 여행 참가 모집 인원이 마감되어 수락할 수 없습니다. enrollNumber: {}", enrollmentNumber);
            throw new IllegalArgumentException("여행 참가 모집 인원이 마감되어 수락할 수 없습니다.");
        }
        enrollment.accepted();

        // 여행 참가자 생성
        Companion newCompanion = Companion.create(targetTravel, enrollment.getUserNumber());
        companionRepository.save(newCompanion);

        //알림
        notificationService.createAcceptNotification(targetTravel, enrollment.getUserNumber());
        if(targetTravel.isFullCompanion()){
            //TODO: 인원 마감 notification -> receiver: 참가자(companion) and 주최자
        }
    }

    @Transactional
    public void reject(long enrollmentNumber, int requestUserNumber) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentNumber)
                .orElseThrow(() -> {
                    log.warn("Enrollment reject - 존재하지 않는 신청입니다. enrollNumber: {}", enrollmentNumber);
                    return new IllegalArgumentException("존재하지 않는 신청입니다.");
                });

        Travel targetTravel = travelRepository.findByNumber(enrollment.getTravelNumber())
                .orElseThrow(() -> {
                    log.warn("Enrollment reject - 존재하지 않는 여행 콘텐츠입니다. travelNumber: {}", enrollment.getTravelNumber());
                    return new IllegalArgumentException("존재하지 않는 여행 콘텐츠입니다.");
                });

        // 여행 주최자 권한 확인
        if (!targetTravel.isTravelHostUser(requestUserNumber)) {
            log.warn("Enrollment reject - 여행 참가 신청 거절 권한이 없습니다. enrollNumber: {}, requestUser:{}", enrollmentNumber, requestUserNumber);
            throw new IllegalArgumentException("여행 참가 신청 거절 권한이 없습니다.");
        }
        enrollment.rejected();

        //알림
        notificationService.createRejectNotification(targetTravel, enrollment.getUserNumber());
    }

}
