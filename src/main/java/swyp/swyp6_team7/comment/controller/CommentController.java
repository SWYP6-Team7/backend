package swyp.swyp6_team7.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.comment.domain.Comment;
import swyp.swyp6_team7.comment.dto.request.CommentCreateRequestDto;
import swyp.swyp6_team7.comment.dto.request.CommentUpdateRequestDto;
import swyp.swyp6_team7.comment.dto.response.CommentDetailResponseDto;
import swyp.swyp6_team7.comment.dto.response.CommentListReponseDto;
import swyp.swyp6_team7.comment.service.CommentService;

import swyp.swyp6_team7.member.service.MemberService;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CommentController {

    private final CommentService commentService;
    private final MemberService memberService;

    //Create
    @PostMapping("/api/{relatedType}/{relatedNumber}/comments")
    public ResponseEntity<CommentDetailResponseDto> create(
            @RequestBody CommentCreateRequestDto request,
            Principal principal,
            @PathVariable(name = "relatedType") String relatedType,
            @PathVariable(name = "relatedNumber") int relatedNumber
    ) {
        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();

        Comment createdComment = commentService.create(request, userNumber, relatedType, relatedNumber);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.getCommentByNumber(createdComment.getCommentNumber()));
    }

    // List Read
    @GetMapping("/api/{relatedType}/{relatedNumber}/comments")
    public ResponseEntity<Page<CommentListReponseDto>> getComments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Principal principal,
            @PathVariable String relatedType,
            @PathVariable int relatedNumber) {

        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();


        Page<CommentListReponseDto> comments = commentService.getListPage(PageRequest.of(page, size), relatedType, relatedNumber, userNumber);
        return ResponseEntity.ok(comments);
    }

    //Update
    @PutMapping("/api/comments/{commentNumber}")
    public ResponseEntity<CommentDetailResponseDto> update(
            @RequestBody CommentUpdateRequestDto request, Principal principal,
            @PathVariable int commentNumber
    ) {
        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();

        CommentDetailResponseDto updateResponse = commentService.update(request, userNumber, commentNumber);

        return ResponseEntity.status(HttpStatus.OK)
                .body(updateResponse);
    }

    //Delete
    @DeleteMapping("/api/comments/{commentNumber}")
    public ResponseEntity<Void> delete(
            @PathVariable int commentNumber,
            Principal principal
    ) {
        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();

        commentService.delete(commentNumber, userNumber);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

