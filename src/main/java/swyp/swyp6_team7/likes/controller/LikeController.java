package swyp.swyp6_team7.likes.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.comment.dto.response.CommentListReponseDto;
import swyp.swyp6_team7.likes.service.LikeService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.util.MemberAuthorizeUtil;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LikeController {

    private final MemberService memberService;
    private final LikeService likeService;
    private final JwtProvider jwtProvider;

    //좋아요
    @PostMapping("/api/{relatedType}/{relatedNumber}/like")
    public ResponseEntity<Object> toggleLike(
            @PathVariable String relatedType, @PathVariable int relatedNumber,
            Principal principal) {

        // user number 가져오기
        int userNumber = MemberAuthorizeUtil.getLoginUserNumber();

        Object result = likeService.toggleLike(relatedType, relatedNumber, userNumber);
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }
}
