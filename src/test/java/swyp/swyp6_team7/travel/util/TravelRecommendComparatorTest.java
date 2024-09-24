package swyp.swyp6_team7.travel.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import swyp.swyp6_team7.travel.dto.TravelRecommendDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TravelRecommendComparatorTest {

    @DisplayName("compareTo: preferredNumber가 큰 쪽이 더 작다")
    @Test
    public void compareTo() {
        // given
        TravelRecommendDto dto1 = TravelRecommendDto.builder()
                .preferredNumber(1)
                .build();
        TravelRecommendDto dto2 = TravelRecommendDto.builder()
                .preferredNumber(5)
                .build();
        List<TravelRecommendDto> result = new ArrayList<>(List.of(dto1, dto2));

        // when
        Collections.sort(result, new TravelRecommendComparator());

        // then
        assertThat(result.get(0)).isEqualTo(dto2);
        assertThat(result.get(1)).isEqualTo(dto1);
    }

    @DisplayName("compareTo: preferredNumber가 같으면 RegisterDue가 작은 쪽(현재와 가까운)이 더 작다")
    @Test
    public void compareToWhenPreferredNumberSame() {
        // given
        TravelRecommendDto dto1 = TravelRecommendDto.builder()
                .preferredNumber(5)
                .registerDue(LocalDate.now().plusDays(5))
                .build();
        TravelRecommendDto dto2 = TravelRecommendDto.builder()
                .preferredNumber(5)
                .registerDue(LocalDate.now().plusDays(1))
                .build();
        List<TravelRecommendDto> result = new ArrayList<>(List.of(dto1, dto2));

        // when
        Collections.sort(result, new TravelRecommendComparator());

        // then
        assertThat(result.get(0)).isEqualTo(dto2);
        assertThat(result.get(1)).isEqualTo(dto1);
    }

}