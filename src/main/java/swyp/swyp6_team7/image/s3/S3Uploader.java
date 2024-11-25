package swyp.swyp6_team7.image.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.image.util.S3KeyHandler;
import swyp.swyp6_team7.image.util.StorageNameHandler;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final AmazonS3 amazonS3;
    private final S3Component s3Component;
    private final S3KeyHandler s3KeyHandler; // s3KeyHandler 추가 - FileFolderHandler
    private final StorageNameHandler storageNameHandler; // storageNameHandler 추가 - FileNameHandler
    private final ImageRepository imageRepository;


    //S3에 파일 업로드 하는 메소드
    public String upload(MultipartFile file, String relatedType, int relatedNumber, int order) throws IOException {

        // 파일 메타데이터
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        //폴더 경로와 파일 이름 생성
        String storageName = storageNameHandler.generateUniqueFileName(file.getOriginalFilename()); // 고유한 파일 이름 생성
        String S3Key = s3KeyHandler.generateS3Key(relatedType, relatedNumber, storageName, order); // 경로 생성

        try (InputStream inputStream = file.getInputStream()) {
            //S3에 파일 업로드
            amazonS3.putObject(new PutObjectRequest(s3Component.getBucket(), S3Key, inputStream, metadata));
        } catch (IOException e) {
            log.warn("Failed to upload file to S3 - relatedType:{}, relatedNumber:{}", relatedType, relatedNumber);
            throw new RuntimeException("Failed to upload file to S3", e); //업로드 실패 시 예외 처리
        }
        // S3Path 리턴
        return S3Key;
    }

    //임시저장 경로에 파일 업로드 하는 메소드
    public String uploadInTemporary(MultipartFile file, String relatedType) {
        // 파일 메타데이터
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        //폴더 경로와 파일 이름 생성
        String storageName = storageNameHandler.generateUniqueFileName(file.getOriginalFilename()); // 고유한 파일 이름 생성
        String S3Key = s3KeyHandler.generateTempS3Key(relatedType, storageName); // 경로 생성

        try (InputStream inputStream = file.getInputStream()) {
            //S3에 파일 업로드
            amazonS3.putObject(new PutObjectRequest(s3Component.getBucket(), S3Key, inputStream, metadata));
        } catch (IOException e) {
            // InputStream 예외 처리
            log.warn("S3 파일 업로드 IOException: {}", e.getMessage());
            throw new RuntimeException("S3 파일 업로드 실패", e);
        } catch (SdkClientException e) {
            // AWS SDK S3 파일 업로드 예외 처리
            log.warn("S3 파일 업로드 SdkClientException 실패 for Key: {}", S3Key);
            throw new RuntimeException("S3 파일 업로드 실패", e);
        }
        // S3Path 리턴
        return S3Key;
    }


    //해당 경로에 파일이 존재하는지 확인하는 메소드
    public boolean existObject(String relatedType, int relatedNumber, int order) {
        String storageName = imageRepository.findByRelatedTypeAndRelatedNumberAndOrder(relatedType, relatedNumber, order).get().getStorageName();
        String key = s3KeyHandler.generateS3Key(relatedType, relatedNumber, storageName, order);

        return amazonS3.doesObjectExist(s3Component.getBucket(), key);
    }

    //해당 경로에 특정 파일이 존재하는지 확인하는 메소드 (key로 확인)
    public boolean existObject(String key) {
        return amazonS3.doesObjectExist(s3Component.getBucket(), key);

    }


    // S3 파일 삭제 메소드
    public void deleteFile(String S3Key) {
        try {
            // 파일이 존재하면 삭제 시도
            if (amazonS3.doesObjectExist(s3Component.getBucket(), S3Key)) {
                amazonS3.deleteObject(s3Component.getBucket(), S3Key);
                System.out.println("파일 삭제 완료: " + S3Key);
            }
            // 존재하지 않으면 넘어감 (아무 로그도 출력하지 않음)
        } catch (Exception e) {
            // 삭제 중 오류 발생 시 로그
            System.err.println("S3 파일 삭제 실패: " + e.getMessage());
        }
    }


    // S3 key 로 URL 추출
    public String getImageUrl(String S3Key) {
        // S3에서 해당 경로에 이미지가 존재하는지 확인
        if (amazonS3.doesObjectExist(s3Component.getBucket(), S3Key)) {
            // 이미지가 존재하면 해당 URL 반환
            return amazonS3.getUrl(s3Component.getBucket(), S3Key).toString();
        } else {
            // 이미지가 없을 경우 빈 문자열 반환
            return "";
        }
    }


    // 이미지 복사 메서드
    public String copyImage(String sourceKey, String destinationKey) {
        CopyObjectRequest copyRequest = new CopyObjectRequest(
                s3Component.getBucket(),    // 소스 버킷 이름
                sourceKey,                 // 소스 경로 (Key)
                s3Component.getBucket(),    // 대상 버킷 이름
                destinationKey             // 대상 경로 (Key)
        );

        // S3에서 복사
        CopyObjectResult result = amazonS3.copyObject(copyRequest);

        //복사 후 경로 리턴
        return destinationKey;
    }


    // 이미지 경로 이동 메서드
    public String moveImage(String sourceKey, String destinationKey) {

        //기존 경로에서 이미지 복사
        copyImage(sourceKey, destinationKey);

        //기존 경로의 이미지 삭제
        deleteFile(sourceKey);

        //경로 이동 후 path 리턴
        return destinationKey;
    }

    //relatedType, relatedNumber, order로 key를 추출하는 메소드
    public String getKey(String relatedType, int relatedNumber, int order) {
        String storageName = imageRepository.findByRelatedTypeAndRelatedNumberAndOrder(relatedType, relatedNumber, order).get().getStorageName();
        String key = s3KeyHandler.generateS3Key(relatedType, relatedNumber, storageName, order);
        return key;
    }

}

