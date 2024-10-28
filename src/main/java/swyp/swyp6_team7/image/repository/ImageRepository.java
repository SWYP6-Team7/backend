package swyp.swyp6_team7.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.image.domain.Image;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    // 관련 타입과 관련 번호로 단일 이미지를 찾는 메소드
    Optional<Image> findByRelatedTypeAndRelatedNumber(String relatedType, int relatedNumber);

    @Query("select i.url from Image i where i.relatedNumber = :userNumber and i.relatedType = 'profile' and i.order = 0")
    Optional<String> findUrlByRelatedUserNumber(@Param("userNumber") int userNumber);

    // 관련 타입이 community일 때 여러 이미지를 찾는 메소드
    List<Image> findAllByRelatedTypeAndRelatedNumber(String relatedType, int relatedNumber);

    //key로 이미지찾기
    Optional<Image> findByRelatedTypeAndRelatedNumberAndKey(String relatedType, int relatedNumber, String key);

    Optional<Image> findByRelatedTypeAndRelatedNumberAndOrder(String relatedType, int relatedNumber, int order);

    Optional<Image> findByKey(String key);

    Optional<Image> findByUrl(String url);


    Optional<Image> findByImageNumber(Long imageNumber);
}
