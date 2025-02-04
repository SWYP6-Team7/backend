package swyp.swyp6_team7.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import swyp.swyp6_team7.image.domain.Image;
import swyp.swyp6_team7.image.dto.ImageCreateDto;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.dto.response.ImageTempResponseDto;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.image.s3.S3Uploader;
import swyp.swyp6_team7.image.util.S3KeyHandler;
import swyp.swyp6_team7.image.util.StorageNameHandler;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3Uploader s3Uploader;
    private final StorageNameHandler storageNameHandler;
    private final S3KeyHandler s3KeyHandler;


    //단일 파일 이미지 업로드 후 DB 저장
    @Transactional
    public ImageDetailResponseDto createByFile(String relatedType, int relatedNumber, int order, MultipartFile file) throws IOException {

        String key = s3Uploader.upload(file, relatedType, relatedNumber, order);
        String savedImageUrl = s3Uploader.getImageUrl(key);

        // 메타 데이터 뽑아서 create dto에 담기
        ImageCreateDto imageCreateDto = ImageCreateDto.builder()
                .originalName(file.getOriginalFilename())
                .storageName(storageNameHandler.generateUniqueFileName(file.getOriginalFilename()))
                .size(file.getSize())
                .format(file.getContentType())
                .relatedType(relatedType)
                .relatedNumber(relatedNumber)
                .order(order)
                .key(key)
                .url(savedImageUrl)
                .build();

        // DB에 저장
        Image image = imageCreateDto.toImageEntity();

        Image uploadedImage = imageRepository.save(image);
        return ImageDetailResponseDto.from(uploadedImage);
    }


    // 이미지 삭제
    @Transactional
    public void deleteImage(String relatedType, int relatedNumber) {
        if ("profile".equals(relatedType)) {
            // 1대1
            Image image = imageRepository.findByRelatedTypeAndRelatedNumber(relatedType, relatedNumber)
                    .orElseThrow(() -> new IllegalArgumentException("이미지 삭제 실패: 해당 이미지를 찾을 수 없습니다." + relatedType + ":" + relatedNumber));
            String presentKey = image.getKey();

            //이전 이미지가 파일 업로드인지 default 이미지인지 확인
            //key가 "images/profile/relatedNumber"로 시작하면, 이전 이미지는 파일 업로드 이미지
            if (presentKey.startsWith("images/profile/" + relatedNumber)) {
                //이미지 삭제
                s3Uploader.deleteFile(presentKey);
                // 데이터베이스에서 이미지 삭제
                imageRepository.delete(image);
            }
            //key가 "images/profile/default"로 시작하면, 이전 이미지는 디폴트 이미지
            else if (presentKey.startsWith("images/profile/default")) {
                //이미지 삭제 동작 필요 없음
            } else {
                throw new IllegalArgumentException("업데이트 전 DB  데이터에 오류가 있습니다.");
            }

        } else if ("community".equals(relatedType)) {
            // 1 대 다
            List<Image> images = imageRepository.findAllByRelatedTypeAndRelatedNumber(relatedType, relatedNumber);
            for (Image image : images) {
                // S3에서 파일 삭제
                s3Uploader.deleteFile(image.getKey());

                // 데이터베이스에서 이미지 삭제
                imageRepository.delete(image);
            }
        } else {
            throw new IllegalArgumentException("유효하지 않는 유형입니다: " + relatedType);
        }
    }

    // 이미지 정보 조회
    public ImageDetailResponseDto getImageDetail(String relatedType, int relatedNumber, int order) {
        Image image = imageRepository.findByRelatedTypeAndRelatedNumberAndOrder(relatedType, relatedNumber, order)
                .orElseThrow(() -> {
                    log.warn("Image Not Found. relatedType: {}, relatedNumber: {}, order: {}", relatedType, relatedNumber, order);
                    return new IllegalArgumentException("이미지를 찾을 수 없습니다.");
                });

        return ImageDetailResponseDto.from(image);
    }

    //임시저장
    public ImageTempResponseDto temporaryImage(MultipartFile file) {
        String relatedType = "profile";

        // 임시 저장 경로로 S3에 업로드
        String tempKey = s3Uploader.uploadInTemporary(file, relatedType);
        String temUrl = s3Uploader.getImageUrl(tempKey);

        return new ImageTempResponseDto(temUrl);
    }

    //임시 저장 삭제
    public void deleteTempImage(String temUrl) {
        String tempKey = s3KeyHandler.getKeyByUrl(temUrl);
        s3Uploader.deleteFile(tempKey);
    }

}
