package swyp.swyp6_team7.image.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.swyp6_team7.image.domain.Image;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ImageDetailResponseDto {

    private Long imageNumber;
    private String relatedType;
    private int relatedNumber;
    private String key;
    private String url;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH시 mm분")
    private LocalDateTime uploadDate;


    @Builder
    public ImageDetailResponseDto(Long imageNumber, String relatedType, int relatedNumber, String key, String url, LocalDateTime uploadDate) {
        this.imageNumber = imageNumber;
        this.relatedType = relatedType;
        this.relatedNumber = relatedNumber;
        this.key = key;
        this.url = url;
        this.uploadDate = uploadDate;
    }


    public static ImageDetailResponseDto from(Image image) {
        return ImageDetailResponseDto.builder()
                .imageNumber(image.getImageNumber())
                .relatedType(image.getRelatedType())
                .relatedNumber(image.getRelatedNumber())
                .key(image.getKey())
                .url(image.getUrl())
                .uploadDate(image.getUploadDate())
                .build();
    }

    @Override
    public String toString() {
        return "ImageDetailResponseDto{" +
                "imageNumber=" + imageNumber +
                ", relatedType='" + relatedType + '\'' +
                ", relatedNumber=" + relatedNumber +
                ", key='" + key + '\'' +
                ", url='" + url + '\'' +
                ", uploadDate=" + uploadDate +
                '}';
    }

}
