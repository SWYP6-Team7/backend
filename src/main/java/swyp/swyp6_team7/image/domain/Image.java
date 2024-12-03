package swyp.swyp6_team7.image.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "Images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_number", updatable = false, nullable = false)
    private Long imageNumber;


    //파일에서 뽑아오는 데이터
    @Column(name = "image_original_name")
    private String originalName;    //실제 파일 이름

    @Column(name = "image_storage_name")
    private String storageName; //서버에 저장된 파일 이름

    @Column(name = "image_size")
    private Long size;  //파일의 크기

    @Column(name = "image_format")
    private String format;  //파일의 포맷


    //관리(구분)을 위한 테이터
    @Column(name = "image_related_type")
    private String relatedType; //어느 기능과 관련된 이미지 인지
    //"profile" 은 프로필 이미지, community 는 커뮤니티 게시물 이미지

    @Column(name = "image_related_number")
    private int relatedNumber;  //누구 혹은 어느 게시물의 이미지인지
    //프로필 이미지면 userNumber 입력, 커뮤니티 이미지면 게시물 번호 입력

    @Column(name = "image_order")
    private int order;
    //프로필 이미지일 경우 0
    //게시글 이미지일 경우 1~3


    //**upload하고 가져오는 데이터
    //S3에 저장 된 이미지 폴더 경로
    @Column(name = "image_key")
    private String key;

    @Column(name = "image_url")
    private String url;

    //이미지 업로드 일시
    @Column(name = "image_upload_date")
    private LocalDateTime uploadDate;

    //파일로 Create
    @Builder
    public Image(String originalName, String storageName, Long size, String format, String relatedType, int relatedNumber, int order, String key, String url, LocalDateTime uploadDate) {
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

    //relatedType이 profile 일 때, 프로필 이미지가 기본이미지 일 경우
    @Builder
    public Image(String relatedType, int relatedNumber, String key, String url) {
        this.relatedType = relatedType;
        this.relatedNumber = relatedNumber;
        this.key = key;
        this.url = url;
    }

    // 각 필드에 대해 입력값이 null이면 해당 필드 데이터 지우고, null이 아닌 경우 수정
    public Image update(
            String originalName, String storageName, Long size, String format,
            String relatedType, int relatedNumber, int order,
            String key, String url, LocalDateTime uploadDate
    ) {
        // 전달된 값이 null이면 기존 데이터를 삭제 (null로 설정)
        this.originalName = (originalName != null) ? originalName : null;
        this.storageName = (storageName != null) ? storageName : null;
        this.size = (size != null) ? size : null;
        this.format = (format != null) ? format : null;
        this.relatedType = relatedType;
        this.relatedNumber = relatedNumber;
        this.order = order;
        this.key = (key != null) ? key : null;
        this.url = url;
        this.uploadDate = uploadDate;
        return this;
    }

    public Image update(int relatedNumber, int order, String key, String url, LocalDateTime uploadDate) {
        this.relatedNumber = relatedNumber;
        this.order = order;
        this.key = key;
        this.url = url;
        this.uploadDate = uploadDate;
        return this;
    }

    public Image updateWithUrl(String key, String url, LocalDateTime uploadDate) {
        this.originalName = null;
        this.storageName = null;
        this.size = null;
        this.format = null;
        this.key = key;
        this.url = url;
        this.uploadDate = uploadDate;
        return this;
    }

}