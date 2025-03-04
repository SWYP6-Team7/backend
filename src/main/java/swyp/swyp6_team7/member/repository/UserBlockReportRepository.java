package swyp.swyp6_team7.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.member.entity.UserBlockReport;

import java.util.List;

@Repository
public interface UserBlockReportRepository extends JpaRepository<UserBlockReport, Integer> {
    List<UserBlockReport> findAllByReportedUserNumberOrderByRegTs(int userNumber);

    List<UserBlockReport> findAllByReportedUserNumberAndReporterUserNumber(int reportedUserNumber, int reporterUserNumber);
}
