package swyp.swyp6_team7.travel.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import swyp.swyp6_team7.travel.dto.TravelRecommendForMemberDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TravelRecommendComparatorTest {

    @DisplayName("compareTo: preferredNumber값이 큰 쪽이 우선 정렬된다.")
    @Test
    public void compareTo() {
        // given
        TravelRecommendForMemberDto dto1 = TravelRecommendForMemberDto.builder()
                .preferredNumber(1)
                .build();
        TravelRecommendForMemberDto dto2 = TravelRecommendForMemberDto.builder()
                .preferredNumber(5)
                .build();
        List<TravelRecommendForMemberDto> result = new ArrayList<>(List.of(dto1, dto2));

        // when
        Collections.sort(result, new TravelRecommendComparator());

        // then
        assertThat(result.get(0)).isEqualTo(dto2);
        assertThat(result.get(1)).isEqualTo(dto1);
    }

    @DisplayName("compareTo: preferredNumber가 같으면 제목순으로 오름차순 정렬한다")
    @Test
    public void compareToWhenDueDateSame() {
        // given
        TravelRecommendForMemberDto dto1 = TravelRecommendForMemberDto.builder()
                .title("나")
                .preferredNumber(5)
                .build();
        TravelRecommendForMemberDto dto2 = TravelRecommendForMemberDto.builder()
                .title("가다")
                .preferredNumber(5)
                .build();
        List<TravelRecommendForMemberDto> result = new ArrayList<>(List.of(dto1, dto2));

        // when
        Collections.sort(result, new TravelRecommendComparator());

        // then
        assertThat(result.get(0)).isEqualTo(dto2);
        assertThat(result.get(1)).isEqualTo(dto1);
    }

}