package swyp.swyp6_team7.member.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.member.entity.*;
import swyp.swyp6_team7.member.repository.DeletedUsersRepository;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberDeletedService {

    private final DeletedUsersRepository deletedUsersRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;

    @Transactional
    public void deleteUserData(Users user, SocialUsers socialUser) {
        log.info("회원 데이터 삭제 요청: userNumber={}", user.getUserNumber());

        try {
            DeletedUsers deletedUser = saveDeletedUser(user); // 탈퇴 회원 정보 저장 (비식별화 처리)
            anonymizeUser(user);
            if (socialUser != null) {
                anonymizeSocialUser(socialUser);
            }
            updateUserContentReferences(user.getUserNumber(), deletedUser); // 콘텐츠 연결 업데이트
            userRepository.save(user);

            log.info("회원 데이터 삭제 완료: userNumber={}", user.getUserNumber());
        } catch (Exception e) {
            log.error("회원 데이터 삭제 중 오류 발생: userNumber={}", user.getUserNumber(), e);
            throw new IllegalStateException("회원 데이터 삭제 처리 중 오류가 발생했습니다.", e);
        }
    }
    private void anonymizeUser(Users user) {
        log.info("사용자 비식별화 처리 시작: userNumber={}", user.getUserNumber());

        user.setUserEmail("deleted@" + user.getUserNumber() + ".com");
        user.setUserName("deletedUser");
        user.setUserPw("");  // 비밀번호는 빈 값으로 처리
        user.setUserGender(Gender.NULL);  // 성별 NULL로 설정
        user.setUserAgeGroup(AgeGroup.UNKNOWN);  // 연령대 UNKNOWN으로 설정
        user.setUserStatus(UserStatus.DELETED);  // 삭제된 사용자 상태로 설정
        log.info("사용자 비식별화 처리 완료: userNumber={}", user.getUserNumber());

    }
    private void anonymizeSocialUser(SocialUsers socialUser) {
        log.info("소셜 사용자 비식별화 처리 시작");

        Users user = socialUser.getUser();
        if (user == null) {
            log.warn("소셜 사용자와 연결된 일반 사용자 정보가 없습니다.");
            throw new IllegalStateException("소셜 사용자와 연결된 일반 사용자 정보가 없습니다.");
        }
        Integer userNumber = user.getUserNumber();
        log.info("소셜 사용자 비식별화 처리 대상: userNumber={}", userNumber);

        socialUser.setSocialEmail("deleted@" + userNumber + ".com");
        socialUser.setSocialLoginId("null");

        log.info("사용자 비식별화 처리 완료: userNumber={}",userNumber);
    }


    private LocalDate calculateFinalDeletionDate(LocalDate deletedUserDeleteDate) {
        return deletedUserDeleteDate.plusMonths(3);  // 3개월 뒤로 설정
    }


    private DeletedUsers saveDeletedUser(Users user) {
        log.info("탈퇴 회원 정보 저장: userNumber={}", user.getUserNumber());

        DeletedUsers deletedUser = new DeletedUsers();
        deletedUser.setUserNumber(user.getUserNumber());
        deletedUser.setDeletedUserEmail(user.getUserEmail());
        deletedUser.setDeletedUserLoginDate(user.getUserLoginDate());
        deletedUser.setDeletedUserDeleteDate(LocalDate.now()); // 현재 탈퇴 시간
        deletedUser.setFinalDeletionDate(calculateFinalDeletionDate(LocalDate.now())); // 3개월 뒤 삭제

        try {
            return deletedUsersRepository.save(deletedUser);
        } catch (Exception e) {
            log.error("탈퇴 회원 정보 저장 중 오류 발생: userNumber={}", user.getUserNumber(), e);
            throw new IllegalStateException("탈퇴 회원 정보 저장 중 오류가 발생했습니다.", e);
        }
    }



    private void updateUserContentReferences(Integer userNumber, DeletedUsers deletedUser) {
        log.info("콘텐츠 참조 업데이트: userNumber={}", userNumber);

        List<Travel> userTravels = travelRepository.findByUserNumber(userNumber);
        for (Travel travel : userTravels) {
            travel.setDeletedUser(deletedUser); // 콘텐츠를 탈퇴한 사용자와 연결
            travelRepository.save(travel);
        }
        log.info("콘텐츠 참조 업데이트 완료: userNumber={}", userNumber);

    }


    @Scheduled(cron = "0 0 2 * * ?")  // 매일 새벽 2시에 실행
    @Transactional
    public void deleteExpiredUsers() {
        log.info("만료된 회원 데이터 삭제 작업 시작");
        List<DeletedUsers> expiredUsers = deletedUsersRepository.findAllByFinalDeletionDateBefore(LocalDate.now());

        for (DeletedUsers deletedUser : expiredUsers) {
            try {
                userRepository.deleteById(deletedUser.getUserNumber());
                deletedUsersRepository.delete(deletedUser);
                log.info("만료된 회원 삭제 성공: userNumber={}", deletedUser.getUserNumber());
            } catch (Exception e) {
                log.error("만료된 회원 삭제 중 오류 발생: userNumber={}", deletedUser.getUserNumber(), e);
            }
        }
        log.info("만료된 회원 데이터 삭제 작업 완료");
    }
    public void validateReRegistration(String email) {
        log.info("재가입 제한 검증 요청: email={}", email);
        Optional<List<DeletedUsers>> deletedUsersOpt = deletedUsersRepository.findAllByDeletedUserEmail(email);

        if (deletedUsersOpt.isEmpty()) {
            log.info("재가입 제한 대상이 아님: email={}", email);
            return;
        }

        List<DeletedUsers> deletedUsers = deletedUsersOpt.get();
        LocalDate mostRecentDeletionDate = deletedUsers.stream()
                .map(DeletedUsers::getFinalDeletionDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        if (mostRecentDeletionDate.isAfter(LocalDate.now())) {
            log.warn("재가입 제한 중 - 3개월 이내 재가입 불가: email={}", email);
            throw new IllegalArgumentException("탈퇴 후 3개월 이내에는 재가입이 불가능합니다.");
        }

        log.info("재가입 제한 없음: email={}", email);
    }
}
