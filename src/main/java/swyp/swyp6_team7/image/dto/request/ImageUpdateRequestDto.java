package swyp.swyp6_team7.image.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ImageUpdateRequestDto {

    private String originalName;  // 원본 파일 이름
    private String storageName;   // 저장된 파일 이름 (고유 이름)
    private Long size;            // 파일 크기 (null 허용)
    private String format;        // 파일 포맷 (MIME 타입)
    private String relatedType;   // 관련 타입 (프로필, 게시물 등)
    private int relatedNumber; // 관련 번호 (userNumber, postNumber 등)
    private int order;        // 순서
    private String key;           // S3에서의 고유 키
    private String url;           // 이미지 URL
    private LocalDateTime uploadDate;  // 업로드 날짜


    // 전체 필드를 받는 빌더
    @Builder
    public ImageUpdateRequestDto(
            String originalName, String storageName, Long size, String format,
            String relatedType, int relatedNumber, int order, String key, String url, LocalDateTime uploadDate) {
        this.originalName = originalName;
        this.storageName = storageName;
        this.size = size;
        this.format = format;
        this.relatedType = relatedType;
        this.relatedNumber = relatedNumber;
        this.order = order;
        this.key = key;
        this.url = url;
        this.uploadDate = uploadDate;
    }

    // 최소 필드를 받는 빌더 (url로 이미지 저장)
    @Builder
    public ImageUpdateRequestDto(String relatedType, int relatedNumber, int order, String key, String url) {
        this.originalName = null;
        this.storageName = null;
        this.size = null;
        this.format = null;
        this.relatedType = relatedType;
        this.relatedNumber = relatedNumber;
        this.order = order;
        this.key = key;
        this.url = url;
        this.uploadDate = LocalDateTime.now();
    }

}
