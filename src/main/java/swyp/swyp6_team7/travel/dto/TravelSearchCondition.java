package swyp.swyp6_team7.travel.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.util.TravelSearchConstant;
import swyp.swyp6_team7.travel.util.TravelSearchSortingType;

import java.util.ArrayList;
import java.util.List;

import swyp.swyp6_team7.location.domain.LocationType;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TravelSearchCondition {

    private PageRequest pageRequest;
    private String keyword;
    private List<LocationType> locationFilter;
    private List<GenderType> genderFilter;
    private List<String> personRangeFilter;
    private List<PeriodType> periodFilter;
    private List<String> tags;
    private TravelSearchSortingType sortingType;

    @Builder
    public TravelSearchCondition(
            PageRequest pageRequest,
            String keyword,
            List<String> locationTypes,
            List<String> genderTypes,
            List<String> personTypes,
            List<String> periodTypes,
            List<String> tags,
            String sortingType
    ) {
        this.pageRequest = pageRequest;
        this.keyword = keyword;
        this.locationFilter = getLocationFilter(locationTypes);
        this.genderFilter = getGenderFilter(genderTypes);
        this.personRangeFilter = getPersonFilter(personTypes);
        this.periodFilter = getPeriodFilter(periodTypes);
        this.tags = getTags(tags);
        this.sortingType = TravelSearchSortingType.of(sortingType);
    }

    private List<LocationType> getLocationFilter(List<String> locationTypes) {
        if (locationTypes == null) {
            return new ArrayList<>();
        }
        return locationTypes.stream()
                .distinct().limit(TravelSearchConstant.LOCATION_TYPE_COUNT)
                .map(this::convertToCityType)
                .toList();
    }

    private LocationType convertToCityType(String locationType) {
        return LocationType.fromString(locationType);
    }


    private List<GenderType> getGenderFilter(List<String> genderTypes) {
        if (genderTypes == null) {
            return new ArrayList<>();
        }
        return genderTypes.stream()
                .distinct().limit(TravelSearchConstant.GENDER_TYPE_COUNT)
                .map(GenderType::of)
                .toList();
    }

    private List<String> getPersonFilter(List<String> personTypes) {
        if (personTypes == null) {
            return new ArrayList<>();
        }
        return personTypes.stream()
                .distinct().limit(TravelSearchConstant.PERSON_TYPE_COUNT)
                .toList();
    }

    private List<PeriodType> getPeriodFilter(List<String> periodTypes) {
        if (periodTypes == null) {
            return new ArrayList<>();
        }
        return periodTypes.stream()
                .distinct().limit(TravelSearchConstant.PERIOD_TYPE_COUNT)
                .map(PeriodType::of)
                .toList();
    }

    private List<String> getTags(List<String> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        return tags.stream()
                .distinct()
                .toList();
    }

    @Override
    public String toString() {
        return "TravelSearchCondition{" +
                "pageRequest=" + pageRequest +
                ", keyword='" + keyword + '\'' +
                ", locationFilter=" + locationFilter +
                ", genderFilter=" + genderFilter +
                ", personRangeFilter=" + personRangeFilter +
                ", periodFilter=" + periodFilter +
                ", tags=" + tags +
                ", sortingType=" + sortingType +
                '}';
    }
}
