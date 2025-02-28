package swyp.swyp6_team7.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.member.dto.ReportDetailReason;
import swyp.swyp6_team7.member.dto.ReportReasonResponse;
import swyp.swyp6_team7.member.dto.UserBlockDetailResponse;
import swyp.swyp6_team7.member.entity.*;
import swyp.swyp6_team7.member.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberBlockService {

    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final UserBlockReportRepository userBlockReportRepository;
    private final ReportReasonRepository reportReasonRepository;
    private final UserBlockExplanationRepository userBlockExplanationRepository;

    public List<ReportReasonResponse> getAllReportReason() {
        List<ReportReason> reportReasons = reportReasonRepository.findAll();

        // 그룹핑된 신고사유 목록
        Map<ReportCategory, List<ReportReason>> groupedReasons = reportReasons.stream()
                .collect(Collectors.groupingBy(ReportReason::getReportCategory));

        return Arrays.stream(ReportCategory.values())
                .map(category -> {
                    // 해당 카테고리에 해당하는 ReportReason 들에서 ID, reason 쌍을 추출
                    List<ReportDetailReason> details = groupedReasons.getOrDefault(category, Collections.emptyList())
                            .stream()
                            .map(reason -> new ReportDetailReason(reason.getId(), reason.getReason()))
                            .collect(Collectors.toList());

                    return new ReportReasonResponse(category.getValue(), details);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public String report(
            Integer reporterUserNumber,
            int reportedUserNumber,
            int reportReasonId,
            String reportReasonExtra
    ) {
        // 1. 피신고자가 받은 모든 신고내역 확인
        List<UserBlockReport> reports = userBlockReportRepository.findAllByReportedUserNumberOrderByRegTs(reportedUserNumber);

        // 2-1. 동일인 신고 여부 확인
        List<UserBlockReport> reportsBySameUser = reports.stream()
                .filter(report -> report.getReporterUserNumber().equals(reporterUserNumber))
                .toList();

        // 기존 요청사항
        // 동일인 & 동일 사유 => 시간 요소 추가. 3회 / 5회
        // 동일인 X & 동일 사유 => 시간 요소 X. 3회 / 5회
        // 동일인 & 다른 사유 => 시간 요소 추가. 5회 / 10회
        // 동일인 X & 다른 사유 => 시간 요소 X. 5회 / 10회

        // 2-2. 동일인 신고 존재 시, 가장 마지막 신고로부터 시점 계산
        int userReportCount = reports.size();
        int sameUserReportCount = reportsBySameUser.size();

        if (sameUserReportCount == 0) {
            log.info("신규 신고가 접수되었습니다. reporter: {}, reported: {}", reporterUserNumber, reportedUserNumber);
            userBlockReportRepository.save(
                    new UserBlockReport(
                            reporterUserNumber,
                            reportedUserNumber,
                            reportReasonId,
                            reportReasonExtra
                    )
            );
            userReportCount++;
        } else {
            UserBlockReport lastReportBySameUser = reportsBySameUser.getLast();
            if (lastReportBySameUser != null) {
                boolean isReportable = canUseReport(sameUserReportCount, lastReportBySameUser.getRegTs());

                if (isReportable) {
                    log.info("신고가 접수되었습니다. reporter: {}, reported: {}", reporterUserNumber, reportedUserNumber);
                    userBlockReportRepository.save(
                            new UserBlockReport(
                                    reporterUserNumber,
                                    reportedUserNumber,
                                    reportReasonId,
                                    reportReasonExtra
                            )
                    );
                    userReportCount++;
                } else {
                    log.info("이미 신고한 내역이 있습니다. 신고처리 할 수 없습니다.");
                    return "이미 신고한 이력이 있습니다.";
                }
            }
        }

        // 4. 신고 내역 추산해서 정지 처리
        if (userReportCount < 5) {
            log.info("신고건이 5건 이하입니다.");
            return "정상 처리되엇습니다.";
        }

        log.info("신고 접수건이 5건 이상입니다. 계정 정지를 진행합니다.");

        List<UserBlock> userBlocks = userBlockRepository.findAllByUserNumberOrderByRegTs(reportedUserNumber)
                .stream().filter(UserBlock::isActive)
                .toList();

        /*
          5회 이상 신고 & 접수내역 없으면 등록
          5회 이상 신고 & 접수내역 있으면 pass
          10회 신고 & 접수내역 없으면 등록
          10회 신고 & 접수내역 있으면 block Type 업데이트 + block 기간 등록
         */
        int blockCountTarget = BlockType.BLOCK.getCount();
        int warnCountTarget = BlockType.WARN.getCount();
        LocalDate blockPeriod = LocalDate.now().plusDays(90);

        if (userReportCount >= blockCountTarget && userBlocks.isEmpty()) {
            // 10회 신고 & 접수내역 없으면 등록
            UserBlock userBlock = new UserBlock(
                    reportedUserNumber,
                    BlockType.BLOCK,
                    true,
                    blockPeriod
            );
            userBlockRepository.save(userBlock);
            saveUserStatusToBlock(reportedUserNumber);
            log.info("계정을 정지합니다. userNumber: {}, blockPeriod: {}", reportedUserNumber, userBlock.getBlockPeriod());
        } else if (userReportCount >= blockCountTarget && userBlocks.size() == 1) {
            // 10회 신고 & 접수내역 있으면 block Type 업데이트 + block 기간 등록
            UserBlock lastBlock = userBlocks.getLast();
            if (lastBlock.getBlockType() == BlockType.WARN) {
                lastBlock.setBlockType(BlockType.BLOCK);
                lastBlock.setBlockPeriod(blockPeriod);
                userBlockRepository.save(lastBlock);
                saveUserStatusToBlock(reportedUserNumber);
                log.info("계정을 정지합니다. userNumber: {}, blockPeriod: {}", reportedUserNumber, lastBlock.getBlockPeriod());
            }
        } else if (userReportCount >= warnCountTarget && userBlocks.isEmpty()) {
            // 5회 이상 신고 & 접수내역 없으면 등록
            UserBlock userBlock = new UserBlock(
                    reportedUserNumber,
                    BlockType.WARN,
                    true,
                    null
            );
            log.info("계정 정지를 경고합니다. userNumber: {}, blockPeriod: {}", reportedUserNumber, userBlock.getBlockPeriod());
            userBlockRepository.save(userBlock);
            // TODO: 알림 발송
        }

        return "정상 처리되었습니다.";
    }

    private void saveUserStatusToBlock(Integer userNumber) {
        Users user = userRepository.findById(userNumber)
                .orElseThrow(() -> new IllegalArgumentException("일반 회원 정보를 찾을 수 없습니다."));

        user.setUserStatus(UserStatus.BLOCK);
        log.info("유저 상태값 Block 으로 변경. userNumber: {}", userNumber);
    }

    // lastReportTs 가 지금보다 {reportGapWeek}주 이전에 신고된 내역인지 확인
    private boolean canUseReport(int reportCount, LocalDateTime lastReportTs) {
        int reportGapWeek;
        if (reportCount < 4) {
            reportGapWeek = Math.max(reportCount, 0);
        } else {
            reportGapWeek = 4;
        }

        boolean isReportable = LocalDateTime.now().minusWeeks(reportGapWeek).isAfter(lastReportTs);
        log.info("reportGapWeek={}, lastReportTs={}, isReportable={}", reportGapWeek, lastReportTs, isReportable);
        return isReportable;
    }

    public UserBlockDetailResponse getBlockDetail(Integer userNumber) {
        Users user = userRepository.findById(userNumber)
                .orElseThrow(() -> new IllegalArgumentException("일반 회원 정보를 찾을 수 없습니다."));

        if (!user.isBlocked()) {
            return new UserBlockDetailResponse(userNumber, false, null, null);
        }

        UserBlock userBlock = userBlockRepository.findAllByUserNumberOrderByRegTs(userNumber)
                .stream().filter(UserBlock::isValidBlock)
                .toList().getLast();

        if (userBlock == null) {
            user.setUserStatus(UserStatus.ABLE);
            userRepository.save(user);
            return new UserBlockDetailResponse(userNumber, false, null, null);
        }

        // TODO: 기간 지난 valid block 들 제거
        // TODO: 신고 사유 어떻게 할지
        return new UserBlockDetailResponse(userNumber, true, null, userBlock.getBlockPeriod());
    }
}
