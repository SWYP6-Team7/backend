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

import java.time.LocalDateTime;
import java.util.List;


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

        //카테고리 명으로 카테고리 번호 가져와서
        int categoryNumber = categoryRepository.findByCategoryName(request.getCategoryName()).getCategoryNumber();

        //DB create 동작
        Community savedPost = communityRepository.save(request.toCommunityEntity(
                userNumber,
                categoryNumber,
                request.getTitle(),
                request.getContent(),
                LocalDateTime.now(), // 등록 일시
                0 // 조회수
        ));


        CommunityDetailResponseDto response = getDetail(savedPost.getPostNumber(), userNumber);
        return response;
    }


    //게시글 상세 조회
    public CommunityDetailResponseDto getDetail(int postNumber, int userNumber) {
        //userNubmer : 조회 요청자의 회원 번호

        //존재하는 게시글인지 확인
        Community community = communityRepository.findByPostNumber(postNumber)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다." + postNumber));

        // 게시글 작성자 명 가져오기
        Users PostWriter = userRepository.findByUserNumber(community.getUserNumber()).get();
        String postWriter = PostWriter.getUserName();

        String CategoryName = categoryRepository.findByCategoryNumber(community.getCategoryNumber()).getCategoryName();

        // 댓글 수 가져오기
        long commentCount = commentRepository.countByRelatedTypeAndRelatedNumber("community", postNumber);

        //좋아요 상태 가져오기
        LikeReadResponseDto likeStatus = LikeStatus.getLikeStatus(likeRepository, "community", postNumber, userNumber);
        long likes = likeStatus.getTotalLikes(); //좋아요 수
        boolean liked = likeStatus.isLiked(); //좋아요 여부

        // 게시글 작성자 프로필 이미지 url 가져오기
        String profileImageUrl = imageService.getImageDetailByNumber("profile", PostWriter.getUserNumber(), 0).getUrl();

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
                .likeCount(likes)                       // 좋아요 수
                .liked(liked)                           // 좋아요 여부
                .profileImageUrl(profileImageUrl)       // 작성자 프로필 이미지 URL
                .build();

        return detailResponse;
    }

    //조회수를 올리면서 게시글 상세 조회를 동시에 처리하는 메소드
    @Transactional
    public CommunityDetailResponseDto increaseView(int postNumber, int userNumber) {

        //조회수 +1
        communityCustomRepository.incrementViewCount(postNumber);

        //게시물 상세보기 데이터 가져오기
        CommunityDetailResponseDto response = getDetail(postNumber, userNumber);

        return response;
    }

    //게시글 수정
    @Transactional
    public CommunityDetailResponseDto update(CommunityUpdateRequestDto request, int postNumber, int userNumber) {

        // 수정할 게시글이 존재하는지 확인
        Community community = communityRepository.findByPostNumber(postNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다: " + postNumber));
        // 본인 게시글인지 확인
        if (community.getUserNumber() == userNumber) {
        } else {
            throw new IllegalArgumentException("본인 게시물이 아닙니다.");
        }

        int categoryNumber = categoryRepository.findByCategoryName(request.getCategoryName()).getCategoryNumber();

        //DB update 동작
        community.update(categoryNumber, request.getTitle(), request.getContent());

        //게시물 상세보기 데이터 가져오기
        CommunityDetailResponseDto response = getDetail(postNumber, userNumber);

        return response;
    }

    //게시글 삭제
    @Transactional
    public void delete(int postNumber, int userNumber) {

        // 삭제할 게시글이 존재하는지 확인
        Community community = communityRepository.findByPostNumber(postNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다: " + postNumber));
        // 본인 게시글인지 확인
        if (community.getUserNumber() == userNumber) {
        } else {
            throw new IllegalArgumentException("본인 게시물이 아닙니다.");
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
    }


}
