package swyp.swyp6_team7.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.swyp6_team7.notice.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long>{
}
