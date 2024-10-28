package swyp.swyp6_team7.community.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.category.domain.QCategory;
import swyp.swyp6_team7.community.domain.QCommunity;
import swyp.swyp6_team7.community.dto.response.CommunitySearchCondition;
import swyp.swyp6_team7.community.dto.response.CommunitySearchDto;
import swyp.swyp6_team7.community.util.CommunitySearchSortingType;
import swyp.swyp6_team7.likes.domain.QLike;
import swyp.swyp6_team7.member.entity.QUsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class CommunityCustomRepositoryImpl implements CommunityCustomRepository {

    private final JPAQueryFactory queryFactory;
    QCommunity community = QCommunity.community;
    QCategory categories = QCategory.category;
    QLike like = QLike.like;
    QUsers users = QUsers.users;


    @Autowired
    public CommunityCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }


    // 조회수 +1 하는 메소드
    @Override
    public void incrementViewCount(int postNumber) {
        queryFactory.update(community)
                .set(community.viewCount, community.viewCount.add(1))
                .where(community.postNumber.eq(postNumber))
                .execute();
    }

//    public List<CommunitySearchDto> search(CommunitySearchCondition searchCondition) {
//        //좋아요 수 집계
//        Map<Integer, Long> likeCountMap = getLikeCountByPostNumbers();
//
//        //게시글과 관련된 정보 + 좋아요 수 매핑
//        List<CommunitySearchDto> content = queryFactory
//                .select(community, categories.categoryName)
//                .from(community)
//                .leftJoin(categories).on(community.categoryNumber.eq(categories.categoryNumber))
//                .where(
//                        keywordContains(searchCondition.getKeyword()),
//                        categoryEquals(searchCondition.getCategoryNumber())
//                )
//                .orderBy(getOrderBy(searchCondition.getSortingType(), likeCountMap).toArray(new OrderSpecifier[0])) // 정렬 조건 추가
//                .fetch()
//                .stream()
//                .map(tuple -> new CommunitySearchDto(
//                        tuple.get(community),
//                        tuple.get(categories.categoryName),
//                        likeCountMap.getOrDefault(tuple.get(community.postNumber), 0L)
//                ))
//                .toList();
//
//        // 3. 전체 게시글 수 조회
//        Long totalCount = queryFactory
//                .select(community.count())
//                .from(community)
//                .where(
//                        keywordContains(searchCondition.getKeyword()),
//                        categoryEquals(searchCondition.getCategoryNumber())
//                )
//                .fetchOne();
//
//        return content; // 페이징 제거 후 전체 결과 반환
//    }

    private Map<Integer, Long> getLikeCountByPostNumbers() {
        List<Tuple> likeCounts = queryFactory
                .select(like.relatedNumber, like.count())
                .from(like)
                .where(like.relatedType.eq("community"))
                .groupBy(like.relatedNumber)
                .fetch();

        // 결과를 Map으로 변환
        Map<Integer, Long> likeCountMap = new HashMap<>();
        for (Tuple tuple : likeCounts) {
            likeCountMap.put(tuple.get(like.relatedNumber), tuple.get(like.count()));
        }
        return likeCountMap;
    }


    // 키워드 검색 조건
    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null) {
            return null;
        }
        // OR 조건으로 title과 content에서 검색
        return community.title.containsIgnoreCase(keyword)
                .or(community.content.containsIgnoreCase(keyword));
    }

    // 카테고리 필터 조건
    private BooleanExpression categoryEquals(Integer categoryNumber) {
        // categoryNumber가 null일 경우 모든 카테고리에 대해 조회
        return categoryNumber != null ? community.categoryNumber.eq(categoryNumber) : community.categoryNumber.isNotNull();
    }


//    // 정렬 조건 설정
//    private List<OrderSpecifier<?>> getOrderBy(CommunitySearchSortingType sortingType, Map<Integer, Long> likeCountMap) {
//        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
//
//        if (sortingType == null) {
//            orderSpecifiers.add(community.regDate.desc());
//        } else {
//            switch (sortingType) {
//                case REG_DATE_DESC: //최신순
//                    orderSpecifiers.add(community.regDate.desc());
//                    break;
//                case REG_DATE_ASC://등록일순
//                    orderSpecifiers.add(community.regDate.asc());
//                    break;
//                case LIKE_COUNT_DESC: //추천순
//                    orderSpecifiers.add(getLikeCountOrderSpecifier(likeCountMap, false));
//                    break;
//                default:
//                    orderSpecifiers.add(community.regDate.desc());
//                    break;
//            }
//        }
//        return orderSpecifiers;
//    }
//
//    // 좋아요 수에 따른 정렬 조건 생성
//    private OrderSpecifier<Long> getLikeCountOrderSpecifier(Map<Integer, Long> likeCountMap, boolean ascending) {
//        // Map에 저장된 좋아요 수를 기준으로 정렬 (커스텀 정렬)
//        return new OrderSpecifier<>(
//                ascending ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC,
//                com.querydsl.core.types.dsl.Expressions.numberTemplate(Long.class,
//                        "function('coalesce', {0}, 0)", likeCountMap)
//        );
//    }

    public List<CommunitySearchDto> search(CommunitySearchCondition searchCondition) {
        // 게시글과 관련된 정보 + 좋아요 수 집계 쿼리
        List<Tuple> content = queryFactory
                .select(
                        community,
                        categories.categoryName,
                        like.count().coalesce(0L) // 좋아요 수 집계
                )
                .from(community)
                .leftJoin(categories).on(community.categoryNumber.eq(categories.categoryNumber))
                .leftJoin(like).on(community.postNumber.eq(like.relatedNumber)
                        .and(like.relatedType.eq("community")))
                .where(
                        keywordContains(searchCondition.getKeyword()),
                        categoryEquals(searchCondition.getCategoryNumber())
                )
                .groupBy(community.postNumber, categories.categoryName) // group by 적용
                .orderBy(getOrderBy(searchCondition.getSortingType()).toArray(new OrderSpecifier[0])) // 가변인자로 변환
                .fetch();

        // 결과를 DTO로 변환
        return content.stream()
                .map(tuple -> new CommunitySearchDto(
                        tuple.get(community),
                        tuple.get(categories.categoryName),
                        tuple.get(like.count().coalesce(0L)) // 좋아요 수 가져오기
                ))
                .toList();
    }

    // 정렬 조건 설정 (좋아요 순 정렬 포함)
    private List<OrderSpecifier<?>> getOrderBy(CommunitySearchSortingType sortingType) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (sortingType == null) {
            orderSpecifiers.add(community.regDate.desc());
        } else {
            switch (sortingType) {
                case REG_DATE_DESC: // 최신순
                    orderSpecifiers.add(community.regDate.desc());
                    break;
                case REG_DATE_ASC: // 등록일순
                    orderSpecifiers.add(community.regDate.asc());
                    break;
                case LIKE_COUNT_DESC: // 추천순(좋아요 순)
                    orderSpecifiers.add(like.count().coalesce(0L).desc());
                    break;
                default:
                    orderSpecifiers.add(community.regDate.desc());
                    break;
            }
        }
        return orderSpecifiers;
    }

    public List<CommunitySearchDto> getMyList(CommunitySearchSortingType sortingType, int userNumber) {
        //userNumber는 조회중인 유저

        // 게시글과 관련된 정보 + 좋아요 수 집계 쿼리
        List<Tuple> content = queryFactory
                .select(
                        community,
                        categories.categoryName,
                        like.count().coalesce(0L) // 좋아요 수 집계
                )
                .from(community)
                .leftJoin(categories).on(community.categoryNumber.eq(categories.categoryNumber))
                .leftJoin(like).on(community.postNumber.eq(like.relatedNumber)
                        .and(like.relatedType.eq("community")))
                .where(
                        community.userNumber.eq(userNumber)
                )                .groupBy(community.postNumber, categories.categoryName) // group by 적용
                .orderBy(getOrderBy(sortingType).toArray(new OrderSpecifier[0])) // 가변인자로 변환
                .fetch();

        // 결과를 DTO로 변환
        return content.stream()
                .map(tuple -> new CommunitySearchDto(
                        tuple.get(community),
                        tuple.get(categories.categoryName),
                        tuple.get(like.count().coalesce(0L)) // 좋아요 수 가져오기
                ))
                .toList();
    }
}
