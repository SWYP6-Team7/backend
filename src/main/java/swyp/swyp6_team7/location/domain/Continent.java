package swyp.swyp6_team7.location.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Continent {
    ASIA("아시아"),
    EUROPE("유럽"),
    NORTH_AMERICA("북아메리카"),
    SOUTH_AMERICA("남아메리카"),
    AFRICA("아프리카"),
    OCEANIA("오세아니아"),
    ANTARCTICA("남극");

    private final String description;

    public static Continent fromString(String value) {
        return Arrays.stream(values())
                .filter(e -> e.description.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원되지 않는 Continent입니다: " + value));
    }

    @Override
    public String toString() {
        return description;
    }
}
