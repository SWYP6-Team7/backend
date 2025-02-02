package swyp.swyp6_team7.likes.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.likes.service.LikeService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    //좋아요
    @PostMapping("/api/{relatedType}/{relatedNumber}/like")
    public ResponseEntity<Object> toggleLike(
            @PathVariable String relatedType, @PathVariable int relatedNumber,
            @RequireUserNumber Integer userNumber
    ) {

        Object result = likeService.toggleLike(relatedType, relatedNumber, userNumber);
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }
}
