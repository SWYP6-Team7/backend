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
import swyp.swyp6_team7.community.dto.response.CommunitySearchCondition;
import swyp.swyp6_team7.community.dto.response.CommunitySearchDto;
import swyp.swyp6_team7.community.repository.CommunityCustomRepository;
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
        // userNumer : 현재 조회 요청하는 사용자

        Page<CommunitySearchDto> searchedCommunities = communityCustomRepository.search(pageRequest, searchCondition);
        log.info("Number of searched communities: {}", searchedCommunities.getTotalElements());
        log.info("Search Condition: {}", searchCondition);



        List<CommunityListResponseDto> responseDtos = searchedCommunities.stream()
                .map(dto -> {
                    Community community = dto.getCommunity();
                    String postWriter = userRepository.findByUserNumber(community.getUserNumber())
                            .map(user -> user.getUserName())
                            .orElse("알 수 없는 사용자");

                    String categoryName = categoryRepository.findByCategoryNumber(community.getCategoryNumber())
                            .getCategoryName();

                    // 댓글 수 가져오기
                    long commentCount = commentRepository.countByRelatedTypeAndRelatedNumber("community", community.getPostNumber());

                    // 좋아요 상태 가져오기
                    LikeReadResponseDto likeStatus = LikeStatus.getLikeStatus(
                            likeRepository, "community", community.getPostNumber(), userNumber);
                    long likeCount = likeStatus.getTotalLikes();
                    boolean liked = likeStatus.isLiked();

                    // 썸네일 이미지 URL 가져오기
                    String thumbnailUrl = imageRepository.findByRelatedTypeAndRelatedNumberAndOrder("community", community.getPostNumber(), 1)
                            .map(image -> image.getUrl())  // 이미지가 존재하는 경우 URL 반환
                            .orElse(null); // 이미지가 없을 경우 null


                    // CommunityListResponseDto 생성
                    return CommunityListResponseDto.fromEntity(
                            community, postWriter, categoryName,
                            commentCount, likeCount, liked, thumbnailUrl
                    );
                })
                .toList();


        return toPage(responseDtos, pageRequest);
    }




        //Page 객체 생성
        private Page<CommunityListResponseDto> toPage(List<CommunityListResponseDto> responses, Pageable pageable) {
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), responses.size());

            if (start > end) {
                start = end;
            }
            return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
        }
    }