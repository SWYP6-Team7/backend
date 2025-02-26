package swyp.swyp6_team7.member.entity;

// 회원 상태 enum으로 관리
public enum UserStatus {
    ABLE,
    DELETED,
    SLEEP,
    PENDING,
    BLOCK; // 계정 정지 상태
    // TODO: WARN 추가여부 고민 (신고횟수 표시용)
}