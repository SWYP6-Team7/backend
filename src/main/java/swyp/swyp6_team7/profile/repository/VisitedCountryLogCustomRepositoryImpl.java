package swyp.swyp6_team7.profile.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.companion.domain.QCompanion;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.domain.QCountry;
import swyp.swyp6_team7.location.domain.QLocation;
import swyp.swyp6_team7.travel.domain.QTravel;

import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class VisitedCountryLogCustomRepositoryImpl implements VisitedCountryLogCustomRepository {

    private final JPAQueryFactory queryFactory;

    QTravel travel = QTravel.travel;
    QLocation location = QLocation.location;
    QCountry country = QCountry.country;
    QCompanion companion = QCompanion.companion;

    @Override
    public List<Tuple> findVisitedCountriesWithContinentByUser(Integer userNumber) {
        LocalDate today = LocalDate.now();

        // 내가 개설한 여행 중 종료된 것
        List<Tuple> createdTravels = queryFactory
                .select(location.country.countryName, location.country.continent, travel.startDate)
                .from(travel)
                .join(travel.location, location)
                .where(
                        travel.userNumber.eq(userNumber),
                        travel.endDate.before(today)
                )
                .distinct()
                .fetch();

        // 내가 companion으로 참가한 여행 중 종료된 것
        List<Tuple> participatedTravels = queryFactory
                .select(location.country.countryName, location.country.continent, travel.startDate)
                .from(companion)
                .join(companion.travel, travel)
                .join(travel.location, location)
                .where(
                        companion.userNumber.eq(userNumber),
                        travel.endDate.before(today)
                )
                .distinct()
                .fetch();

        Set<Tuple> result = new LinkedHashSet<>();
        result.addAll(createdTravels);
        result.addAll(participatedTravels);

        return new ArrayList<>(result);
    }

    @Override
    public List<Tuple> findVisitedCountriesWithStartDate(Integer userNumber) {
        LocalDate today = LocalDate.now();

        List<Tuple> created = queryFactory
                .select(
                        country.countryName,
                        country.continent,
                        country
                )
                .from(travel)
                .join(travel.location, location)
                .join(location.country, country)
                .where(
                        travel.userNumber.eq(userNumber),
                        travel.endDate.before(today)
                )
                .distinct()
                .fetch();

        List<Tuple> participated = queryFactory
                .select(
                        country.countryName,
                        country.continent,
                        country
                )
                .from(companion)
                .join(companion.travel, travel)
                .join(travel.location, location)
                .join(location.country, country)
                .where(
                        companion.userNumber.eq(userNumber),
                        travel.endDate.before(today)
                )
                .distinct()
                .fetch();

        Set<Tuple> result = new LinkedHashSet<>();
        result.addAll(created);
        result.addAll(participated);

        return new ArrayList<>(result);
    }
    @Override
    public List<Tuple> findInternationalVisits(Integer userNumber) {
        LocalDate today = LocalDate.now();

        return queryFactory
                .select(country.countryName, country.continent, travel.startDate)
                .from(travel)
                .join(travel.location, location)
                .join(location.country, country)
                .where(
                        travel.userNumber.eq(userNumber)
                                .or(travel.in(
                                        queryFactory
                                                .select(companion.travel)
                                                .from(companion)
                                                .where(companion.userNumber.eq(userNumber))
                                )),
                        location.locationType.eq(LocationType.INTERNATIONAL),
                        travel.endDate.before(today)
                )
                .fetch();
    }

    @Override
    public List<Tuple> findDomesticVisits(Integer userNumber) {
        LocalDate today = LocalDate.now();

        return queryFactory
                .select(location.locationName, travel.startDate)
                .from(travel)
                .join(travel.location, location)
                .where(
                        travel.userNumber.eq(userNumber)
                                .or(travel.in(
                                        queryFactory
                                                .select(companion.travel)
                                                .from(companion)
                                                .where(companion.userNumber.eq(userNumber))
                                )),
                        location.locationType.eq(LocationType.DOMESTIC),
                        travel.endDate.before(today)
                )
                .fetch();
    }
}
