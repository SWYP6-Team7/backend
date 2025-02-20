package swyp.swyp6_team7.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.comment.domain.Comment;
import swyp.swyp6_team7.comment.dto.request.CommentCreateRequestDto;
import swyp.swyp6_team7.comment.dto.request.CommentUpdateRequestDto;
import swyp.swyp6_team7.comment.dto.response.CommentDetailResponseDto;
import swyp.swyp6_team7.comment.dto.response.CommentListReponseDto;
import swyp.swyp6_team7.comment.service.CommentService;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CommentController {

    private final CommentService commentService;

    //Create
    @Operation(summary = "댓글 생성 or 대댓글 생성. parentNumber 가 없으면 신규 댓글, 있으면 대댓글")
    @PostMapping("/api/{relatedType}/{relatedNumber}/comments")
    public ApiResponse<CommentDetailResponseDto> create(
            @RequestBody CommentCreateRequestDto request,
            @PathVariable(name = "relatedType") String relatedType,
            @PathVariable(name = "relatedNumber") int relatedNumber,
            @RequireUserNumber Integer userNumber
    ) {
        // 1. 댓글 or 대댓글 생성

        // 2. 댓글의 좋아요 등의 값 포함해서 응답
        try {
            Comment createdComment = commentService.create(request, userNumber, relatedType, relatedNumber);
            log.info("댓글 생성 성공 - 댓글 번호: {}, 사용자 번호: {}", createdComment.getCommentNumber(), userNumber);

            return ApiResponse.success(commentService.getCommentByNumber(createdComment.getCommentNumber()));
        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청 데이터 - 타입: {}, 게시물 번호: {}, 오류: {}", relatedType, relatedNumber, e.getMessage());
            throw new MoingApplicationException("잘못된 요청 데이터입니다.");
        } catch (Exception e) {
            log.error("댓글 생성 중 예외 발생", e);
            throw new MoingApplicationException("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    // List Read
    @GetMapping("/api/{relatedType}/{relatedNumber}/comments")
    public ApiResponse<Page<CommentListReponseDto>> getComments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @PathVariable(name = "relatedType") String relatedType,
            @PathVariable(name = "relatedNumber") int relatedNumber,
            @RequireUserNumber Integer userNumber
    ) {
        Page<CommentListReponseDto> comments = commentService.getListPage(PageRequest.of(page, size), relatedType, relatedNumber, userNumber);
        log.info("댓글 목록 조회 성공 - 총 댓글 수: {}", comments.getTotalElements());
        return ApiResponse.success(comments);
    }

    //Update
    @PutMapping("/api/comments/{commentNumber}")
    public ApiResponse<CommentDetailResponseDto> update(
            @RequestBody CommentUpdateRequestDto request,
            @PathVariable(name = "commentNumber") int commentNumber,
            @RequireUserNumber Integer userNumber
    ) {
        CommentDetailResponseDto updateResponse = commentService.update(request, userNumber, commentNumber);

        log.info("댓글 수정 성공 - 댓글 번호: {}, 사용자 번호: {}", commentNumber, userNumber);
        return ApiResponse.success(updateResponse);
    }

    //Delete
    @DeleteMapping("/api/comments/{commentNumber}")
    public ApiResponse<Void> delete(
            @PathVariable(name = "commentNumber") int commentNumber,
            @RequireUserNumber Integer userNumber
    ) {
        commentService.delete(commentNumber, userNumber);

        log.info("댓글 삭제 성공 - 댓글 번호: {}, 사용자 번호: {}", commentNumber, userNumber);
        return ApiResponse.success(null);
    }
}