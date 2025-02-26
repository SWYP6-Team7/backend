package swyp.swyp6_team7.member.entity;

public enum ReportCategory {
    OFFENSIVE_CONTENT("욕설, 비방, 혐오"),
    SPAM("도배, 홍보, 영리목적"),
    DECEPTIVE_CONTENT("허위 정보나 사기, 거짓 후기"),
    ILLEGAL_CONTENT("저작권 침해, 불법적인 거래 유도"),
    PRIVACY_SECURITY_VIOLATION("개인정보 노출 및 안전 위협"),
    OTHER("기타");

    private final String value;

    ReportCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
