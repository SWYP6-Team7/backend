package swyp.swyp6_team7.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.comment.domain.Comment;
import swyp.swyp6_team7.comment.dto.request.CommentCreateRequestDto;
import swyp.swyp6_team7.comment.dto.request.CommentUpdateRequestDto;
import swyp.swyp6_team7.comment.dto.response.CommentDetailResponseDto;
import swyp.swyp6_team7.comment.dto.response.CommentListReponseDto;
import swyp.swyp6_team7.comment.service.CommentService;

import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.util.MemberAuthorizeUtil;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CommentController {

    private final CommentService commentService;
    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    //Create
    @PostMapping("/api/{relatedType}/{relatedNumber}/comments")
    public ResponseEntity<?> create(
            @RequestBody CommentCreateRequestDto request,
            Principal principal,
            @PathVariable(name = "relatedType") String relatedType,
            @PathVariable(name = "relatedNumber") int relatedNumber
    ) {
        try {
            Integer userNumber = MemberAuthorizeUtil.getLoginUserNumber();
            Comment createdComment = commentService.create(request, userNumber, relatedType, relatedNumber);
            log.info("댓글 생성 성공 - 댓글 번호: {}, 사용자 번호: {}", createdComment.getCommentNumber(), userNumber);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(commentService.getCommentByNumber(createdComment.getCommentNumber()));
        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청 데이터 - 타입: {}, 게시물 번호: {}, 오류: {}", relatedType, relatedNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 요청 데이터입니다.");
        } catch (Exception e) {
            log.error("댓글 생성 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    // List Read
    @GetMapping("/api/{relatedType}/{relatedNumber}/comments")
    public ResponseEntity<?> getComments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Principal principal,
            @PathVariable(name = "relatedType") String relatedType,
            @PathVariable(name = "relatedNumber") int relatedNumber) {

        try {
            Integer userNumber = MemberAuthorizeUtil.getLoginUserNumber();
            Page<CommentListReponseDto> comments = commentService.getListPage(PageRequest.of(page, size), relatedType, relatedNumber, userNumber);
            log.info("댓글 목록 조회 성공 - 총 댓글 수: {}", comments.getTotalElements());
            return ResponseEntity.ok(comments);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청 데이터 - 페이지: {}, 크기: {}, 타입: {}, 게시물 번호: {}, 오류: {}", page, size, relatedType, relatedNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("요청 데이터가 잘못되었습니다.");
        } catch (Exception e) {
            log.error("댓글 목록 조회 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    //Update
    @PutMapping("/api/comments/{commentNumber}")
    public ResponseEntity<?> update(
            @RequestBody CommentUpdateRequestDto request, Principal principal,
            @PathVariable(name = "commentNumber") int commentNumber
    ) {
        try {
            Integer userNumber = MemberAuthorizeUtil.getLoginUserNumber();
            CommentDetailResponseDto updateResponse = commentService.update(request, userNumber, commentNumber);

            log.info("댓글 수정 성공 - 댓글 번호: {}, 사용자 번호: {}", commentNumber, userNumber);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(updateResponse);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청 데이터 - 댓글 번호: {}, 오류: {}", commentNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("요청 데이터가 잘못되었습니다.");
        } catch (Exception e) {
            log.error("댓글 수정 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    //Delete
    @DeleteMapping("/api/comments/{commentNumber}")
    public ResponseEntity<?> delete(
            @PathVariable(name = "commentNumber") int commentNumber,
            Principal principal
    ) {
        try {
            Integer userNumber = MemberAuthorizeUtil.getLoginUserNumber();
            commentService.delete(commentNumber, userNumber);

            log.info("댓글 삭제 성공 - 댓글 번호: {}, 사용자 번호: {}", commentNumber, userNumber);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청 데이터 - 댓글 번호: {}, 오류: {}", commentNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("요청 데이터가 잘못되었습니다.");
        } catch (Exception e) {
            log.error("댓글 삭제 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}