package swyp.swyp6_team7.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.member.dto.ReportDetailReason;
import swyp.swyp6_team7.member.dto.ReportReasonResponse;
import swyp.swyp6_team7.member.entity.*;
import swyp.swyp6_team7.member.repository.*;

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

    private UserRepository userRepository;
    private UserBlockRepository userBlockRepository;
    private UserBlockReportRepository userBlockReportRepository;
    private ReportReasonRepository reportReasonRepository;
    private UserBlockExplanationRepository userBlockExplanationRepository;

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
    public void report(
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

        // 2-2. 동일인 신고 존재 시, 가장 마지막 신고로부터 시점 계산
        UserBlockReport lastReportBySameUser = reportsBySameUser.getLast();
        int sameUserReportCount = reportsBySameUser.size();
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
                sameUserReportCount++;
            }
        } else { // 3. 타인 신고건이면 신고 바로 접수
            userBlockReportRepository.save(
                    new UserBlockReport(
                            reporterUserNumber,
                            reportedUserNumber,
                            reportReasonId,
                            reportReasonExtra
                    )
            );
            sameUserReportCount++;
        }

        // 4. 신고 내역 추산해서 정지 처리
        if (sameUserReportCount < 3) {
            log.info("신고건이 3건 이하입니다.");
            return;
        }

        List<UserBlock> userBlocks = userBlockRepository.findAllByUserNumberOrderByRegTs(reportedUserNumber)
                .stream().filter(UserBlock::isActive)
                .toList();

        /**
         * 5회 이상 신고 & 접수내역 없으면 등록
         * 5회 이상 신고 & 접수내역 있으면 pass
         * 10회 신고 & 접수내역 없으면 등록
         * 10회 신고 & 접수내역 있으면 block Type 업데이트 + block 기간 등록
         */
        int blockCountTarget = BlockType.BLOCK.getCount();
        int warnCountTarget = BlockType.WARN.getCount();
        LocalDateTime blockPeriod = LocalDateTime.now().plusDays(90);

        if (sameUserReportCount >= blockCountTarget && userBlocks.isEmpty()) {
            // 10회 신고 & 접수내역 없으면 등록
            UserBlock userBlock = new UserBlock(
                    reportedUserNumber,
                    BlockType.BLOCK,
                    true,
                    blockPeriod
            );
            userBlockRepository.save(userBlock);
            saveUserStatusToBlock(reportedUserNumber);
        } else if (sameUserReportCount >= blockCountTarget && userBlocks.size() == 1) {
            // 10회 신고 & 접수내역 있으면 block Type 업데이트 + block 기간 등록
            UserBlock lastBlock = userBlocks.getLast();
            if (lastBlock.getBlockType() == BlockType.WARN) {
                lastBlock.setBlockType(BlockType.BLOCK);
                lastBlock.setBlockPeriod(blockPeriod);
                userBlockRepository.save(lastBlock);
                saveUserStatusToBlock(reportedUserNumber);
            }
        } else if (sameUserReportCount >= warnCountTarget && userBlocks.isEmpty()) {
            // 5회 이상 신고 & 접수내역 없으면 등록
            UserBlock userBlock = new UserBlock(
                    reportedUserNumber,
                    BlockType.WARN,
                    true,
                    null
            );
            userBlockRepository.save(userBlock);
        }
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
        if (reportCount < 5) {
            reportGapWeek = Math.max(reportCount - 1, 0);
        } else {
            reportGapWeek = 4;
        }

        boolean isReportable = LocalDateTime.now().minusWeeks(reportGapWeek).isAfter(lastReportTs);
        log.info("reportGapWeek={}, lastReportTs={}, isReportable={}", reportGapWeek, lastReportTs, isReportable);
        return isReportable;
    }
}
