package swyp.swyp6_team7.member.entity;

public enum BlockType {
    WARN(5), // 5회 신고 접수되어 경고됨
    BLOCK(10); // 10회 이상 신고 접수되어 계정 차단됨

    private final int count;

    BlockType(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
