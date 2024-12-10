package swyp.swyp6_team7.likes.util;

import swyp.swyp6_team7.likes.dto.response.LikeReadResponseDto;
import swyp.swyp6_team7.likes.repository.LikeRepository;

public class LikeStatus {


    // 댓글 조회 시 필요한 좋아요 상태
    public static LikeReadResponseDto getLikeStatus(LikeRepository likeRepository, String relatedType, int relatedNumber, Integer  userNumber) {
        if (userNumber == null) {
            return new LikeReadResponseDto(relatedType,relatedNumber,false, likeRepository.countByRelatedTypeAndRelatedNumber(relatedType, relatedNumber));
        }
        // 좋아요 여부 확인
        boolean liked = likeRepository.existsByRelatedTypeAndRelatedNumberAndUserNumber(relatedType, relatedNumber, userNumber);
        // 총 좋아요 수 가져오기
        long likes = likeRepository.countByRelatedTypeAndRelatedNumber(relatedType, relatedNumber);
        // DTO 생성 및 반환
        return new LikeReadResponseDto(relatedType, relatedNumber, liked, likes);
    }
}
