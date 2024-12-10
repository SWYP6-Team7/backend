package swyp.swyp6_team7.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.category.repository.CategoryRepository;
import swyp.swyp6_team7.comment.domain.Comment;
import swyp.swyp6_team7.comment.repository.CommentRepository;
import swyp.swyp6_team7.comment.service.CommentService;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.dto.request.CommunityCreateRequestDto;
import swyp.swyp6_team7.community.dto.request.CommunityUpdateRequestDto;
import swyp.swyp6_team7.community.dto.response.CommunityDetailResponseDto;
import swyp.swyp6_team7.community.repository.CommunityCustomRepository;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.image.service.ImageService;
import swyp.swyp6_team7.likes.dto.response.LikeReadResponseDto;
import swyp.swyp6_team7.likes.repository.LikeRepository;
import swyp.swyp6_team7.likes.util.LikeStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.category.domain.Category;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommunityService {
    private final CategoryRepository categoryRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ImageService imageService;
    private final CommunityCustomRepository communityCustomRepository;
    private final CommentService commentService;


    //게시글 작성
    @Transactional
    public CommunityDetailResponseDto create(CommunityCreateRequestDto request, int userNumber) {
        try {
            log.info("커뮤니티 게시글 작성 요청: userNumber={}, request={}", userNumber, request);
            //카테고리 명으로 카테고리 번호 가져와서
            int categoryNumber = categoryRepository.findByCategoryName(request.getCategoryName())
                    .map(Category::getCategoryNumber)
                    .orElse(null);

            //DB create 동작
            Community savedPost = communityRepository.save(request.toCommunityEntity(
                    userNumber,
                    categoryNumber,
                    request.getTitle(),
                    request.getContent(),
                    LocalDateTime.now(), // 등록 일시
                    0 // 조회수
            ));

            log.info("게시글 생성 완료: postNumber={}, userNumber={}", savedPost.getPostNumber(), userNumber);
            CommunityDetailResponseDto response = getDetail(savedPost.getPostNumber(), userNumber);
            return response;
        } catch (Exception e) {
            log.error("[커뮤니티] 게시글 작성 중 오류 발생: userNumber={}, request={}", userNumber, request, e);
            throw new RuntimeException("게시글 작성에 실패했습니다.", e);
        }
    }


    //게시글 상세 조회
    public CommunityDetailResponseDto getDetail(int postNumber, Integer userNumber) {
        //userNubmer : 조회 요청자의 회원 번호
        try {
            log.info("게시글 상세 조회 요청: postNumber={}, userNumber={}", postNumber, userNumber);
            //존재하는 게시글인지 확인
            Community community = communityRepository.findByPostNumber(postNumber)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다." + postNumber));
            // 게시글 작성자 명 가져오기
            Users PostWriter = userRepository.findByUserNumber(community.getUserNumber())
                    .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다. userNumber=" + community.getUserNumber()));
            String postWriter = PostWriter.getUserName();
            String CategoryName = categoryRepository.findByCategoryNumber(community.getUserNumber())
                    .map(Category::getCategoryName)
                    .orElse(null);
            // 댓글 수 가져오기
            long commentCount = commentRepository.countByRelatedTypeAndRelatedNumber("community", postNumber);
            // 좋아요 상태 가져오기 (비회원인 경우 좋아요 여부는 false로 처리)
            LikeReadResponseDto likeStatus = (userNumber != null)
                    ? LikeStatus.getLikeStatus(likeRepository, "community", postNumber, userNumber)
                    : new LikeReadResponseDto("community",postNumber,false, 0);

            // 게시글 작성자 프로필 이미지 url 가져오기
            String profileImageUrl = imageService.getImageDetail("profile", PostWriter.getUserNumber(), 0).getUrl();

            //데이터 Dto에 담기
            CommunityDetailResponseDto detailResponse = CommunityDetailResponseDto.builder()
                    .postNumber(community.getPostNumber())   // 게시글 번호
                    .userNumber(community.getUserNumber())   // 작성자 유저 번호
                    .postWriter(postWriter)                 // 작성자명
                    .categoryNumber(community.getCategoryNumber())  // 카테고리 번호
                    .categoryName(CategoryName)             // 카테고리명
                    .title(community.getTitle())            // 게시글 제목
                    .content(community.getContent())        // 게시글 내용
                    .regDate(community.getRegDate())        // 게시글 등록일시
                    .commentCount(commentCount)             // 댓글 수
                    .viewCount(community.getViewCount())    // 조회수
                    .likeCount(likeStatus.getTotalLikes())  // 좋아요 수
                    .liked(likeStatus.isLiked())            // 좋아요 여부
                    .profileImageUrl(profileImageUrl)       // 작성자 프로필 이미지 URL
                    .build();

            log.info("게시글 상세 조회 완료: postNumber={}, userNumber={}", postNumber, userNumber);
            return detailResponse;
        } catch (Exception e) {
            log.error("[커뮤니티] 게시글 상세 조회 중 오류 발생: postNumber={}, userNumber={}", postNumber, userNumber, e);
            throw new RuntimeException("게시글 상세 조회에 실패했습니다.", e);
        }
    }

    //조회수를 올리면서 게시글 상세 조회를 동시에 처리하는 메소드
    @Transactional
    public CommunityDetailResponseDto increaseView(int postNumber, Integer userNumber) {
        //조회수 +1
        communityCustomRepository.incrementViewCount(postNumber);
        //게시물 상세보기 데이터 가져오기
        CommunityDetailResponseDto response = getDetail(postNumber, userNumber);
        return response;
    }

    //게시글 수정
    @Transactional
    public CommunityDetailResponseDto update(CommunityUpdateRequestDto request, int postNumber, int userNumber) {
        try {
            log.info("게시글 수정 요청: postNumber={}, userNumber={}", postNumber, userNumber);

            // 수정할 게시글이 존재하는지 확인
            Community community = communityRepository.findByPostNumber(postNumber)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다: " + postNumber));
            // 본인 게시글인지 확인
            if (community.getUserNumber() != userNumber) {
                throw new IllegalArgumentException("본인 게시물이 아닙니다.");
            }

            int categoryNumber = categoryRepository.findByCategoryName(request.getCategoryName())
                    .map(Category::getCategoryNumber) // Category가 Optional로 감싸진 경우에만 사용 가능
                    .orElse(null);

            //DB update 동작
            community.update(categoryNumber, request.getTitle(), request.getContent());

            //게시물 상세보기 데이터 가져오기
            CommunityDetailResponseDto response = getDetail(postNumber, userNumber);
            log.info("게시글 수정 완료: postNumber={}, userNumber={}", postNumber, userNumber);

            return response;
        } catch (Exception e) {
            log.error("[커뮤니티] 게시글 수정 중 오류 발생: postNumber={}, userNumber={}", postNumber, userNumber, e);
            throw new RuntimeException("게시글 수정에 실패했습니다.", e);
        }
    }

    //게시글 삭제
    @Transactional
    public void delete(int postNumber, int userNumber) {
        try {
            log.info("게시글 삭제 요청: postNumber={}, userNumber={}", postNumber, userNumber);

            // 삭제할 게시글이 존재하는지 확인
            Community community = communityRepository.findByPostNumber(postNumber)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다: " + postNumber));
            // 본인 게시글인지 확인
            if (community.getUserNumber() != userNumber) {
                throw new IllegalArgumentException("본인 게시물이 아닙니다. userNumber=" + userNumber);
            }
            //좋아요 기록 삭제
            likeRepository.deleteByRelatedTypeAndRelatedNumber("community", postNumber);
            //댓글 삭제
            List<Comment> comments = commentRepository.findByRelatedTypeAndRelatedNumber("community", postNumber);
            for (Comment comment : comments) {
                commentService.delete(comment.getCommentNumber(), userNumber);
            }
            //게시글 이미지 삭제
            imageService.deleteImage("community", postNumber);
            // 게시글 삭제
            communityRepository.delete(community);
            log.info("게시글 삭제 완료: postNumber={}, userNumber={}", postNumber, userNumber);
        } catch (Exception e) {
            log.error("게시글 삭제 중 오류 발생: postNumber={}, userNumber={}", postNumber, userNumber, e);
            throw new RuntimeException("게시글 삭제에 실패했습니다.", e);
        }
    }


}
