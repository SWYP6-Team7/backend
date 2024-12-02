package swyp.swyp6_team7.image.dto;

import lombok.Builder;
import lombok.Getter;
import swyp.swyp6_team7.image.domain.Image;

import java.time.LocalDateTime;

@Getter
public class ImageCreateDto {

    private String originalName; // 원본 파일 이름
    private String storageName; // 저장된 파일 이름 (고유 이름)
    private long size; // 파일 크기
    private String format; // 파일 포맷 (MIME 타입)
    private String relatedType; // 관련 타입 (프로필, 게시물 등)
    private int relatedNumber; // 관련 번호 (userNumber, postNumber 등)
    private int order;
    private String key;
    private String url;
    private LocalDateTime uploadDate;

    @Builder
    public ImageCreateDto(
            String originalName, String storageName, long size, String format,
            String relatedType, int relatedNumber, int order, String key, String url, LocalDateTime uploadDate
    ) {
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

    // 최소 필드만 받는 생성자 (프로필 이미지 기본 이미지용)
    @Builder
    public ImageCreateDto(String relatedType, int relatedNumber, int order, String key, String url) {
        this.relatedType = relatedType;
        this.relatedNumber = relatedNumber;
        this.order = order;
        this.key = key;
        this.url = url;
        this.uploadDate = LocalDateTime.now();
    }

    //임시저장시 사용하는 생성자 (커뮤니티)
    @Builder
    public ImageCreateDto(
            String originalName, String storageName, long size, String format,
            String relatedType, String key, String url
    ) {
        this.originalName = originalName;
        this.storageName = storageName;
        this.size = size;
        this.format = format;
        this.relatedType = relatedType;
        this.key = key;
        this.url = url;
        this.uploadDate = LocalDateTime.now();
    }

    //TODO: Image에 create static method로 수정
    //DB저장
    public Image toImageEntity() {
        return Image.builder()
                .originalName(originalName)
                .storageName(storageName)
                .size(size)
                .format(format)
                .relatedType(relatedType)
                .relatedNumber(relatedNumber)
                .order(order)
                .key(key)
                .url(url)
                .uploadDate(LocalDateTime.now()) // uploadDate는 현재 시간으로 설정
                .build();
    }
}
