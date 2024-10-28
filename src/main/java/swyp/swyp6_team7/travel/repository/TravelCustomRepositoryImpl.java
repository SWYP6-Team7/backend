package swyp.swyp6_team7.travel.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.util.StringUtils;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.bookmark.entity.QBookmark;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.domain.QLocation;
import swyp.swyp6_team7.member.entity.QUsers;
import swyp.swyp6_team7.tag.domain.QTag;
import swyp.swyp6_team7.tag.domain.QTravelTag;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.QTravel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.QTravelDetailDto;
import swyp.swyp6_team7.travel.dto.TravelDetailDto;
import swyp.swyp6_team7.travel.dto.TravelRecommendDto;
import swyp.swyp6_team7.travel.dto.TravelSearchCondition;
import swyp.swyp6_team7.travel.dto.response.TravelRecentDto;
import swyp.swyp6_team7.travel.dto.response.TravelSearchDto;
import swyp.swyp6_team7.travel.util.TravelSearchConstant;
import swyp.swyp6_team7.travel.util.TravelSearchSortingType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;

@Slf4j
@Repository
public class TravelCustomRepositoryImpl implements TravelCustomRepository {

    private final JPAQueryFactory queryFactory;

    public TravelCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    QTravel travel = QTravel.travel;
    QUsers users = QUsers.users;
    QTag tag = QTag.tag;
    QTravelTag travelTag = QTravelTag.travelTag;
    QBookmark bookmark = QBookmark.bookmark;
    QLocation location = QLocation.location;

    @Override
    public TravelDetailDto getDetailsByNumber(int travelNumber, Integer loginUserNumber) {
        return queryFactory
                .select(travel)
                .from(travel)
                .leftJoin(users).on(travel.userNumber.eq(users.userNumber))
                .leftJoin(travel.travelTags, travelTag)
                .leftJoin(travelTag.tag, tag)
                .leftJoin(bookmark).on(bookmark.userNumber.eq(loginUserNumber)
                        .and(bookmark.travelNumber.eq(travel.number)))
                .where(travel.number.eq(travelNumber))
                .transform(groupBy(travel.number).as(new QTravelDetailDto(
                        travel,
                        users.userNumber,
                        users.userName,
                        users.userAgeGroup,
                        travel.companions.size(),
                        list(tag.name),
                        bookmark.bookmarkId.isNotNull()
                ))).get(travelNumber);
    }


    @Override
    public Page<TravelRecentDto> findAllSortedByCreatedAt(PageRequest pageRequest, Integer loginUserNumber) {

        List<Integer> travels = queryFactory
                .select(travel.number)
                .from(travel)
                .where(
                        statusInProgress()
                )
                .orderBy(travel.createdAt.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        List<TravelRecentDto> content = queryFactory
                .select(travel)
                .from(travel)
                .leftJoin(users).on(travel.userNumber.eq(users.userNumber))
                .leftJoin(travel.travelTags, travelTag)
                .leftJoin(travelTag.tag, tag)
                .leftJoin(bookmark).on(bookmark.userNumber.eq(loginUserNumber)
                        .and(bookmark.travelNumber.eq(travel.number)))
                .where(
                        travel.number.in(travels)
                )
                .orderBy(travel.createdAt.desc())
                .transform(groupBy(travel.number).list(
                        Projections.constructor(TravelRecentDto.class,
                                travel,
                                users.userNumber,
                                users.userName,
                                travel.companions.size(),
                                list(tag.name),
                                bookmark.bookmarkId.isNotNull()
                        ))
                );

        JPAQuery<Long> countQuery = queryFactory
                .select(travel.number.countDistinct())
                .from(travel)
                .where(
                        statusActivated()
                );

        return PageableExecutionUtils.getPage(content, pageRequest, countQuery::fetchOne);
    }

    @Override
    public Page<TravelRecommendDto> findAllByPreferredTags(PageRequest pageRequest, Integer loginUserNumber, List<String> preferredTags) {

        NumberExpression<Long> matchingTagCount = new CaseBuilder()
                .when(travel.travelTags.isEmpty()).then(Expressions.nullExpression())
                .when(tag.name.in(preferredTags)).then(Expressions.constant(1L))
                .otherwise(Expressions.nullExpression()).count();

        List<Tuple> tuples = queryFactory
                .select(
                        travel.number,
                        matchingTagCount
                )
                .from(travel)
                .leftJoin(travel.travelTags, travelTag)
                .leftJoin(travelTag.tag, tag)
                .where(
                        statusInProgress()
                )
                .groupBy(travel.number)
                .orderBy(
                        matchingTagCount.desc(),
                        travel.dueDate.asc()
                )
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();
        //log.info("tuples: " + tuples);

        List<Integer> travels = tuples.stream()
                .map(t -> t.get(travel.number))
                .toList();
        //log.info("travelsNumber: " + travels);

        Map<Integer, Integer> travelMap = new HashMap<>();
        for (Tuple tuple : tuples) {
            travelMap.put(tuple.get(travel.number), tuple.get(matchingTagCount).intValue());
        }


        List<TravelRecommendDto> content = queryFactory
                .select(travel)
                .from(travel)
                .leftJoin(users).on(travel.userNumber.eq(users.userNumber))
                .leftJoin(travel.travelTags, travelTag)
                .leftJoin(travelTag.tag, tag)
                .leftJoin(bookmark).on(bookmark.userNumber.eq(loginUserNumber)
                        .and(bookmark.travelNumber.eq(travel.number)))
                .where(
                        travel.number.in(travels)
                )
                .transform(groupBy(travel.number).list(
                        Projections.constructor(TravelRecommendDto.class,
                                travel,
                                users.userNumber,
                                users.userName,
                                travel.companions.size(),
                                list(tag.name),
                                bookmark.bookmarkId.isNotNull()
                        ))
                );
        content.stream()
                .forEach(dto -> dto.updatePreferredNumber(travelMap.get(dto.getTravelNumber())));

        JPAQuery<Long> countQuery = queryFactory
                .select(travel.number.countDistinct())
                .from(travel)
                .where(
                        statusActivated()
                );

        return PageableExecutionUtils.getPage(content, pageRequest, countQuery::fetchOne);
    }


    @Override
    public Page<TravelSearchDto> search(TravelSearchCondition condition, Integer loginUserNumber) {
        List<Integer> travels = queryFactory
                .select(travel.number)
                .from(travel)
                .leftJoin(travel.travelTags, travelTag)
                .leftJoin(travelTag.tag, tag)
                .leftJoin(bookmark).on(bookmark.travelNumber.eq(travel.number))
                .leftJoin(location).on(travel.location.id.eq(location.id))
                .where(
                        titleAndLocationLike(condition.getKeyword()),
                        statusActivated(),
                        eqGenderTypes(condition.getGenderFilter()),
                        eqPersonRangeType(condition.getPersonRangeFilter()),
                        eqPeriodType(condition.getPeriodFilter()),
                        eqTags(condition.getTags()),
                        eqLocationType(condition.getLocationFilter())
                )
                .groupBy(travel.number)
                .having(tag.name.count().goe((long) condition.getTags().size()))
                .orderBy(getOrderSpecifier(condition.getSortingType()).stream()
                        .toArray(OrderSpecifier[]::new))
                .offset(condition.getPageRequest().getOffset())
                .limit(condition.getPageRequest().getPageSize())
                .fetch();
        log.info("search result: " + travels.toString());

        List<TravelSearchDto> content = queryFactory
                .select(travel)
                .from(travel)
                .leftJoin(users).on(travel.userNumber.eq(users.userNumber))
                .leftJoin(travel.travelTags, travelTag)
                .leftJoin(travelTag.tag, tag)
                .leftJoin(bookmark).on(travel.number.eq(bookmark.travelNumber))
                .where(
                        travel.number.in(travels)
                )
                .orderBy(orderCaseBuilder(travels))
                .transform(groupBy(travel.number).list(
                        Projections.constructor(TravelSearchDto.class,
                                travel,
                                users.userNumber,
                                users.userName,
                                travel.companions.size(),
                                list(tag.name),
                                bookmark.userNumber.eq(loginUserNumber))
                ));

        JPAQuery<Long> countQuery = queryFactory
                .select(travel.number.countDistinct())
                .from(travel)
                .leftJoin(travel.travelTags, travelTag)
                .leftJoin(travelTag.tag, tag)
                .leftJoin(bookmark).on(bookmark.travelNumber.eq(travel.number))
                .leftJoin(location).on(travel.location.id.eq(location.id))
                .where(
                        titleAndLocationLike(condition.getKeyword()),
                        statusActivated(),
                        eqGenderTypes(condition.getGenderFilter()),
                        eqPersonRangeType(condition.getPersonRangeFilter()),
                        eqPeriodType(condition.getPeriodFilter()),
                        eqTags(condition.getTags()),
                        eqLocationType(condition.getLocationFilter())
                );

        return PageableExecutionUtils.getPage(content, condition.getPageRequest(), countQuery::fetchOne);
    }


    /**
     * Where절 BooleanExpression
     */
    private BooleanExpression titleAndLocationLike(String keyword) {
        if (StringUtils.isNullOrEmpty(keyword)) {
            return null;
        }
        return travel.title.contains(keyword).or(location.locationName.like("%" + keyword + "%"));
    }

    private BooleanExpression statusActivated() {
        return travel.status.eq(TravelStatus.IN_PROGRESS)
                .or(travel.status.eq(TravelStatus.CLOSED));
    }

    private BooleanExpression statusInProgress() {
        return travel.status.eq(TravelStatus.IN_PROGRESS);
    }

    private BooleanExpression eqGenderTypes(List<GenderType> genderCondition) {
        if (genderCondition.isEmpty() || genderCondition.size() == TravelSearchConstant.GENDER_TYPE_COUNT) {
            return null;
        }
        return travel.genderType.in(genderCondition);
    }

    public BooleanExpression eqPersonRangeType(List<String> personTypes) {
        if (personTypes.isEmpty() || personTypes.size() == TravelSearchConstant.PERSON_TYPE_COUNT) {
            return null;
        }

        if (personTypes.size() == 1) {
            return getPersonRangeBooleanExpression(personTypes.get(0));
        } else {
            return getPersonRangeBooleanExpression(personTypes.get(0)).or(getPersonRangeBooleanExpression(personTypes.get(1)));
        }
    }

    private BooleanExpression getPersonRangeBooleanExpression(String personType) {
        if (personType.equals(TravelSearchConstant.PERSON_TYPE_SMALL)) {
            return travel.maxPerson.loe(2);
        } else if (personType.equals(TravelSearchConstant.PERSON_TYPE_MIDDLE)) {
            return travel.maxPerson.between(3, 4);
        } else if (personType.equals(TravelSearchConstant.PERSON_TYPE_LARGE)) {
            return travel.maxPerson.goe(5);
        } else {
            throw new IllegalArgumentException("잘못된 person filtering 조건입니다.");
        }
    }

    private BooleanExpression eqPeriodType(List<PeriodType> periodCondition) {
        if (periodCondition.isEmpty() || periodCondition.size() == TravelSearchConstant.PERIOD_TYPE_COUNT) {
            return null;
        }
        return travel.periodType.in(periodCondition);
    }

    private BooleanExpression eqTags(List<String> tags) {
        if (tags.isEmpty()) {
            return null;
        }
        return tag.name.in(tags);
    }

    private BooleanExpression eqLocationType(List<LocationType> locationFilter) {
        if (locationFilter == null || locationFilter.isEmpty()) {
            return null;
        }
        return travel.location.id.in( // 수정: travel.location.id로 변경
                JPAExpressions
                        .select(location.id)
                        .from(location)
                        .where(location.locationType.in(locationFilter.stream().toList()))
        );
    }

    private List<OrderSpecifier<?>> getOrderSpecifier(TravelSearchSortingType sortingType) {
        if (sortingType == null) {
            return List.of(travel.dueDate.asc());
        }
        switch (sortingType) {
            case RECOMMEND:
                return List.of(bookmark.bookmarkId.count().desc(), travel.viewCount.desc());
            case CREATED_AT_DESC:
                return List.of(travel.createdAt.desc());
            case CREATED_AT_ASC:
                return List.of(travel.createdAt.asc());
            default:
                return List.of(travel.dueDate.asc());
        }
    }

    private OrderSpecifier orderCaseBuilder(List<Integer> travels) {
        if (travels.size() > 1) {
            return new CaseBuilder()
                    .when(travel.number.eq(travels.get(0))).then(0)
                    .when(travel.number.eq(travels.get(1))).then(1)
                    .otherwise(travels.size())
                    .asc();
        } else if (travels.size() > 0) {
            return new CaseBuilder()
                    .when(travel.number.eq(travels.get(0))).then(0)
                    .otherwise(travels.size())
                    .asc();
        } else {
            return travel.dueDate.asc();
        }
    }
}
