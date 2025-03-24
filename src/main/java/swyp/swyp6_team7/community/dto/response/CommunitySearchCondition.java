package swyp.swyp6_team7.community.dto.response;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import swyp.swyp6_team7.community.util.CommunitySearchSortingType;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CommunitySearchCondition {

    private String keyword;
    @Nullable
    private Integer categoryNumber;
    private CommunitySearchSortingType sortingType;

    @Builder
    public CommunitySearchCondition(
            PageRequest pageRequest,
            String keyword,
            Integer categoryNumber,
            String sortingType
    ) {
        this.keyword = keyword;
        this.categoryNumber = categoryNumber;
        this.sortingType = CommunitySearchSortingType.valueOf(sortingType);
    }
}
