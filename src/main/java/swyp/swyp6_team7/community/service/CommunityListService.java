package swyp.swyp6_team7.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.category.repository.CategoryRepository;
import swyp.swyp6_team7.comment.repository.CommentRepository;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.dto.response.CommunityListResponseDto;
import swyp.swyp6_team7.community.dto.response.CommunityMyListResponseDto;
import swyp.swyp6_team7.community.dto.response.CommunitySearchCondition;
import swyp.swyp6_team7.community.dto.response.CommunitySearchDto;
import swyp.swyp6_team7.community.repository.CommunityCustomRepository;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.community.util.CommunitySearchSortingType;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.likes.dto.response.LikeReadResponseDto;
import swyp.swyp6_team7.likes.repository.LikeRepository;
import swyp.swyp6_team7.likes.util.LikeStatus;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityListService {


    private final CommunityCustomRepository communityCustomRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ImageRepository imageRepository;

    @Transactional(readOnly = true)
    public Page<CommunityListResponseDto> getCommunityList(PageRequest pageRequest, CommunitySearchCondition searchCondition, int userNumber) {
        try {
            log.info("게시물 목록 조회 요청: searchCondition={}, userNumber={}", searchCondition, userNumber);

            List<CommunitySearchDto> searchedCommunities = communityCustomRepository.search(searchCondition);
            log.info("검색된 게시물 수: {}", searchedCommunities.size());

            List<CommunityListResponseDto> responseDtos = searchedCommunities.stream()
                    .map(dto -> {
                        try {
                            Community community = dto.getCommunity();

                            String postWriter = userRepository.findByUserNumber(community.getUserNumber())
                                    .map(user -> user.getUserName())
                                    .orElse("알 수 없는 사용자");

                            String categoryName = categoryRepository.findByCategoryNumber(community.getCategoryNumber())
                                    .getCategoryName();

                            long commentCount = commentRepository.countByRelatedTypeAndRelatedNumber("community", community.getPostNumber());

                            LikeReadResponseDto likeStatus = LikeStatus.getLikeStatus(
                                    likeRepository, "community", community.getPostNumber(), userNumber);
                            long likeCount = likeStatus.getTotalLikes();
                            boolean liked = likeStatus.isLiked();

                            String thumbnailUrl = imageRepository.findByRelatedTypeAndRelatedNumberAndOrder("community", community.getPostNumber(), 1)
                                    .map(image -> image.getUrl())
                                    .orElse(null);

                            return CommunityListResponseDto.fromEntity(
                                    community, postWriter, categoryName,
                                    commentCount, likeCount, liked, thumbnailUrl
                            );
                        } catch (Exception e) {
                            log.error("게시물 응답 생성 중 오류 발생: dto={}", dto, e);
                            throw new RuntimeException("게시물 응답 생성 중 오류가 발생했습니다.", e);
                        }
                    })
                    .toList();

            return toPage(responseDtos, pageRequest);
        } catch (Exception e) {
            log.error("게시물 목록 조회 중 오류 발생: searchCondition={}, userNumber={}", searchCondition, userNumber, e);
            throw new RuntimeException("게시물 목록 조회에 실패했습니다.", e);
        }
    }

    //List Page 객체 생성
    private Page<CommunityListResponseDto> toPage(List<CommunityListResponseDto> responses, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());

        if (start > end) {
            start = end;
        }
        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    @Transactional(readOnly = true)
    public Page<CommunityMyListResponseDto> getMyCommunityList(PageRequest pageRequest, String sortingTypeName, int userNumber) {
        try {
            log.info("내 게시물 목록 조회 요청: sortingTypeName={}, userNumber={}", sortingTypeName, userNumber);

            CommunitySearchSortingType sortingType = parseSortingType(sortingTypeName);

            List<CommunitySearchDto> myCommunities = communityCustomRepository.getMyList(sortingType, userNumber);
            log.info("내 게시물 수: {}", myCommunities.size());

            List<CommunityMyListResponseDto> responseDtos = myCommunities.stream()
                    .map(dto -> {
                        try {
                            Community community = dto.getCommunity();

                            String postWriter = userRepository.findByUserNumber(community.getUserNumber())
                                    .map(user -> user.getUserName())
                                    .orElse("알 수 없는 사용자");

                            String categoryName = categoryRepository.findByCategoryNumber(community.getCategoryNumber())
                                    .getCategoryName();

                            long commentCount = commentRepository.countByRelatedTypeAndRelatedNumber("community", community.getPostNumber());

                            LikeReadResponseDto likeStatus = LikeStatus.getLikeStatus(
                                    likeRepository, "community", community.getPostNumber(), userNumber);
                            long likeCount = likeStatus.getTotalLikes();
                            boolean liked = likeStatus.isLiked();

                            String thumbnailUrl = imageRepository.findByRelatedTypeAndRelatedNumberAndOrder("community", community.getPostNumber(), 1)
                                    .map(image -> image.getUrl())
                                    .orElse(null);

                            long totalCount = myCommunities.size();

                            return CommunityMyListResponseDto.fromEntity(
                                    community, postWriter, categoryName,
                                    commentCount, likeCount, liked, thumbnailUrl, totalCount
                            );
                        } catch (Exception e) {
                            log.error("내 커뮤니티 게시물 응답 생성 중 오류 발생: dto={}", dto, e);
                            throw new RuntimeException("내 커뮤니티 게시물 응답 생성 중 오류가 발생했습니다.", e);
                        }
                    })
                    .toList();

            return toPageForMyList(responseDtos, pageRequest);
        } catch (Exception e) {
            log.error("내 커뮤니티 게시물 목록 조회 중 오류 발생: sortingTypeName={}, userNumber={}", sortingTypeName, userNumber, e);
            throw new RuntimeException("내 커뮤니티 게시물 목록 조회에 실패했습니다.", e);
        }
    }

    private Page<CommunityMyListResponseDto> toPageForMyList(List<CommunityMyListResponseDto> responses, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());

        if (start > end) {
            start = end;
        }
        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    private CommunitySearchSortingType parseSortingType(String sortingTypeName) {
        try {
            return CommunitySearchSortingType.valueOf(sortingTypeName);
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 정렬 유형: sortingTypeName={}, 기본값으로 설정합니다.", sortingTypeName);
            return CommunitySearchSortingType.REG_DATE_DESC;
        }
    }
}