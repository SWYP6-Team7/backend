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
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.notification.service.NotificationService;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.dto.response.TravelEnrollmentsResponse;
import swyp.swyp6_team7.travel.repository.TravelRepository;

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
    public void create(EnrollmentCreateRequest request, int requestUserNumber) {
        Travel targetTravel = travelRepository.findByNumber(request.getTravelNumber())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 여행 콘텐츠입니다: travelNumber={}", request.getTravelNumber());
                    return new MoingApplicationException("존재하지 않는 여행 콘텐츠입니다.");
                });

        if (!targetTravel.availableForEnroll()) {
            log.warn("참가 신청 할 수 없는 여행 콘텐츠입니다: travelNumber={}", targetTravel.getNumber());
            throw new MoingApplicationException("참가 신청 할 수 없는 여행 콘텐츠입니다.");
        }
        // TODO: PENDING 상태의 신청이 이미 존재하는지 확인

        try {
            Enrollment created = Enrollment.create(requestUserNumber, request.getTravelNumber(), request.getMessage());
            enrollmentRepository.save(created);
            log.info("여행 신청 생성 완료: enrollmentNumber={}", created.getNumber());
        } catch (Exception e) {
            log.warn("여행 신청 생성 중 오류 발생: {}", e.getMessage());
            throw new MoingApplicationException("여행 신청 도중 오류가 발생했습니다.");
        }

        //알림
        notificationService.createEnrollNotification(targetTravel, requestUserNumber);
    }

    @Transactional
    public void delete(long enrollmentNumber, int requestUserNumber) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentNumber)
                .orElseThrow(() -> {
                    log.warn("신청이 존재하지 않습니다: enrollNumber={}", enrollmentNumber);
                    return new MoingApplicationException("신청이 존재하지 않습니다.");
                });

        if (enrollment.getUserNumber() != requestUserNumber) {
            log.warn("여행 참가 신청 취소 권한이 없습니다: enrollNumber={}, requestUser={}", enrollmentNumber, requestUserNumber);
            throw new MoingApplicationException("여행 참가 신청 취소 권한이 없습니다.");
        }

        enrollmentRepository.delete(enrollment);
    }

    public TravelEnrollmentsResponse findEnrollmentsByTravelNumber(int travelNumber, int requestUserNumber) {
        Travel targetTravel = travelRepository.findByNumber(travelNumber)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 여행 콘텐츠입니다: travelNumber={}", travelNumber);
                    return new MoingApplicationException("존재하지 않는 여행 콘텐츠입니다.");
                });

        if (!targetTravel.isTravelHostUser(requestUserNumber)) {
            log.warn("여행 참가 신청 목록 조회에 대한 권한이 없습니다. travelNumber: {}, requestUser:{}", travelNumber, requestUserNumber);
            throw new MoingApplicationException("여행 참가 신청 목록 조회에 대한 권한이 없습니다.");
        }

        List<EnrollmentResponse> enrollments = enrollmentRepository.findEnrollmentsByTravelNumber(travelNumber);
        return TravelEnrollmentsResponse.from(enrollments);
    }

    public long getPendingEnrollmentsCountByTravelNumber(int travelNumber) {
        if (!travelRepository.existsTravelByNumber(travelNumber)) {
            log.warn("존재하지 않는 여행 콘텐츠입니다: travelNumber={}", travelNumber);
            throw new MoingApplicationException("존재하지 않는 여행 콘텐츠입니다.");
        }

        return enrollmentRepository.countByTravelNumberAndStatus(travelNumber, EnrollmentStatus.PENDING);
    }

    @Transactional
    public void accept(long enrollmentNumber, int requestUserNumber) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentNumber)
                .orElseThrow(() -> {
                    log.warn("신청이 존재하지 않습니다: enrollNumber={}", enrollmentNumber);
                    return new MoingApplicationException("신청이 존재하지 않습니다.");
                });

        Travel targetTravel = travelRepository.findByNumber(enrollment.getTravelNumber())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 여행 콘텐츠입니다: travelNumber={}", enrollment.getTravelNumber());
                    return new MoingApplicationException("존재하지 않는 여행 콘텐츠입니다.");
                });

        // 여행 주최자 권한 확인
        if (!targetTravel.isTravelHostUser(requestUserNumber)) {
            log.warn("여행 참가 신청 수락 권한이 없습니다: enrollNumber={}, requestUser={}", enrollmentNumber, requestUserNumber);
            throw new MoingApplicationException("여행 참가 신청 수락 권한이 없습니다.");
        }

        // 여행 참가 모집 인원 확인
        if (!targetTravel.availableForAddCompanion()) {
            log.warn("여행 참가 모집 인원이 마감되어 수락할 수 없습니다: enrollNumber={}", enrollmentNumber);
            throw new MoingApplicationException("여행 참가 모집 인원이 마감되어 수락할 수 없습니다.");
        }

        try {
            enrollment.accepted(); // Status 변경

            // 여행 참가자 생성
            Companion newCompanion = Companion.create(targetTravel, enrollment.getUserNumber());
            companionRepository.save(newCompanion);

            log.info("여행 신청 수락 완료: enrollmentNumber={}, newCompanion={}", enrollment.getNumber(), newCompanion.getNumber());
        } catch (Exception e){
            log.warn("여행 신청 수락 중 오류 발생: {}", e.getMessage());
            throw new MoingApplicationException("여행 신청 수락 도중 오류가 발생했습니다.");
        }

        notificationService.createAcceptNotification(targetTravel, enrollment.getUserNumber()); //참가 수락 알림
        if (targetTravel.isFullCompanion()) {
            targetTravel.close();
            notificationService.createCompanionClosedNotification(targetTravel); //참가 인원에 의한 여행 마감 알림
        }
    }

    @Transactional
    public void reject(long enrollmentNumber, int requestUserNumber) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentNumber)
                .orElseThrow(() -> {
                    log.warn("신청이 존재하지 않습니다: enrollNumber={}", enrollmentNumber);
                    return new MoingApplicationException("신청이 존재하지 않습니다.");
                });

        Travel targetTravel = travelRepository.findByNumber(enrollment.getTravelNumber())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 여행 콘텐츠입니다: travelNumber={}", enrollment.getTravelNumber());
                    return new MoingApplicationException("존재하지 않는 여행 콘텐츠입니다.");
                });

        // 여행 주최자 권한 확인
        if (!targetTravel.isTravelHostUser(requestUserNumber)) {
            log.warn("여행 참가 신청 거절 권한이 없습니다: enrollNumber={}, requestUser={}", enrollmentNumber, requestUserNumber);
            throw new MoingApplicationException("여행 참가 신청 거절 권한이 없습니다.");
        }
        enrollment.rejected();

        //알림
        notificationService.createRejectNotification(targetTravel, enrollment.getUserNumber());
    }

}
