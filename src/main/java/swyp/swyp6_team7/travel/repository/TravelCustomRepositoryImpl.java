package swyp.swyp6_team7.travel.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.util.StringUtils;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.member.entity.QUsers;
import swyp.swyp6_team7.tag.domain.QTag;
import swyp.swyp6_team7.tag.domain.QTravelTag;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.QTravel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.TravelSearchCondition;
import swyp.swyp6_team7.travel.dto.response.QTravelDetailResponse;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;
import swyp.swyp6_team7.travel.dto.response.TravelRecentDto;
import swyp.swyp6_team7.travel.dto.response.TravelSearchDto;
import swyp.swyp6_team7.travel.util.TravelSearchConstant;

import java.util.ArrayList;
import java.util.List;

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


    @Override
    public TravelDetailResponse getDetailsByNumber(int travelNumber) {
        return queryFactory
                .select(travel)
                .from(travel)
                .leftJoin(users).on(travel.userNumber.eq(users.userNumber))
                .leftJoin(travel.travelTags, travelTag)
                .leftJoin(travelTag.tag, tag)
                .where(travel.number.eq(travelNumber))
                .transform(groupBy(travel.number).as(new QTravelDetailResponse(
                        travel,
                        users.userNumber,
                        users.userName,
                        list(tag.name)
                ))).get(travelNumber);
    }


    @Override
    public Page<TravelRecentDto> findAllSortedByCreatedAt(PageRequest pageRequest) {

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
                .where(
                        travel.number.in(travels)
                )
                .orderBy(travel.createdAt.desc())
                .transform(groupBy(travel.number).list(
                        Projections.constructor(TravelRecentDto.class,
                                travel,
                                users.userNumber,
                                users.userName,
                                list(tag.name)))
                );

        JPAQuery<Long> countQuery = queryFactory
                .select(travel.count())
                .from(travel)
                .where(
                        statusActivated()
                );

        return PageableExecutionUtils.getPage(content, pageRequest, countQuery::fetchOne);
    }


    @Override
    public Page<TravelSearchDto> search(TravelSearchCondition condition) {
        List<Integer> travels = queryFactory
                .select(travel.number)
                .from(travel)
                .leftJoin(travel.travelTags, travelTag)
                .leftJoin(travelTag.tag, tag)
                .where(
                        titleLike(condition.getKeyword()),
                        statusActivated(),
                        eqGenderTypes(condition.getGenderFilter()),
                        eqPersonRangeType(condition.getPersonRangeFilter()),
                        eqPeriodType(condition.getPeriodFilter()),
                        eqTags(condition.getTags())
                )
                .groupBy(travel.number)
                .having(tag.name.count().goe((long) condition.getTags().size()))
                .orderBy(travel.createdAt.desc())
                .offset(condition.getPageRequest().getOffset())
                .limit(condition.getPageRequest().getPageSize())
                .fetch();

        List<TravelSearchDto> content = queryFactory
                .select(travel)
                .from(travel)
                .leftJoin(users).on(travel.userNumber.eq(users.userNumber))
                .leftJoin(travel.travelTags, travelTag)
                .leftJoin(travelTag.tag, tag)
                .where(
                        travel.number.in(travels)
                )
                .orderBy(travel.createdAt.desc())
                .transform(groupBy(travel.number).list(
                        Projections.constructor(TravelSearchDto.class,
                                travel,
                                users.userNumber,
                                users.userName,
                                list(tag.name)))
                );


        JPAQuery<Long> countQuery = queryFactory
                .select(travel.count())
                .from(travel)
                .leftJoin(travel.travelTags, travelTag)
                .leftJoin(travelTag.tag, tag)
                .where(
                        titleLike(condition.getKeyword()),
                        statusActivated(),
                        eqGenderTypes(condition.getGenderFilter()),
                        eqPersonRangeType(condition.getPersonRangeFilter()),
                        eqPeriodType(condition.getPeriodFilter()),
                        eqTags(condition.getTags())
                );

        return PageableExecutionUtils.getPage(content, condition.getPageRequest(), countQuery::fetchOne);
    }


    /**
     * Where절 BooleanExpression
     */
    private BooleanExpression titleLike(String keyword) {
        if (StringUtils.isNullOrEmpty(keyword)) {
            return null;
        }
        return travel.title.contains(keyword);
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
            if (personTypes.get(0).equals(TravelSearchConstant.PERSON_TYPE_SMALL)) {
                return travel.maxPerson.loe(2);
            } else if (personTypes.get(0).equals(TravelSearchConstant.PERSON_TYPE_MIDDLE)) {
                return travel.maxPerson.between(3, 4);
            } else {
                return travel.maxPerson.goe(5);
            }
        } else {
            if (personTypes.containsAll(new ArrayList<>(List.of(TravelSearchConstant.PERSON_TYPE_SMALL, TravelSearchConstant.PERSON_TYPE_MIDDLE)))) {
                return travel.maxPerson.loe(2).or(travel.maxPerson.between(3, 4));
            } else if (personTypes.containsAll(new ArrayList<>(List.of(TravelSearchConstant.PERSON_TYPE_SMALL, TravelSearchConstant.PERSON_TYPE_LARGE)))) {
                return travel.maxPerson.loe(2).or(travel.maxPerson.goe(5));
            } else {
                return travel.maxPerson.between(3, 4).or(travel.maxPerson.goe(5));
            }
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

}
