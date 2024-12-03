package swyp.swyp6_team7.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.image.domain.Image;
import swyp.swyp6_team7.image.dto.ImageCreateDto;
import swyp.swyp6_team7.image.dto.request.ImageUpdateRequestDto;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.image.s3.S3Uploader;
import swyp.swyp6_team7.image.util.S3KeyHandler;
import swyp.swyp6_team7.image.util.StorageNameHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ImageCommunityService {

    private final ImageRepository imageRepository;
    private final CommunityRepository communityRepository;
    private final ImageService imageService;
    private final S3Uploader s3Uploader;
    private final StorageNameHandler storageNameHandler;
    private final S3KeyHandler s3KeyHandler;


    // 커뮤니티 이미지 임시 저장
    @Transactional
    public ImageDetailResponseDto uploadTempImage(MultipartFile file) {
        String relatedType = "community";

        // S3 임시 경로에 이미지 업로드
        String key = s3Uploader.uploadInTemporary(file, relatedType);
        String savedImageUrl = s3Uploader.getImageUrl(key);

        // 메타 데이터 뽑아서 create DTO로 변환(relatedNumber, order는 0)
        ImageCreateDto imageTempCreateDto = ImageCreateDto.builder()
                .originalName(file.getOriginalFilename())
                .storageName(storageNameHandler.extractStorageName(key))
                .size(file.getSize())
                .format(file.getContentType())
                .relatedType(relatedType)
                .key(key)
                .url(savedImageUrl)
                .uploadDate(LocalDateTime.now())
                .build();

        Image uploadedImage = imageRepository.save(imageTempCreateDto.toImageEntity());
        return ImageDetailResponseDto.from(uploadedImage);
    }


    // 커뮤니티 이미지 정식 저장
    @Transactional
    public List<ImageDetailResponseDto> saveCommunityImages(int relatedNumber, List<String> deletedTempUrls, List<String> tempUrls) {

        // 임시 저장 삭제: 임시 저장 했지만 최종 게시물 등록에는 포함되지 않은 이미지 처리
        if (deletedTempUrls != null && !deletedTempUrls.isEmpty()) {
            deletedTempUrls.stream()
                    .forEach(deletedTempUrl -> deleteCommunityImage(deletedTempUrl));
            log.info("임시 이미지 삭제 완료 deletedTempUrls: {}", deletedTempUrls);
        }

        // 정식 등록: 커뮤니티 게시글에 최종 포함된 이미지 처리
        if (tempUrls != null && !tempUrls.isEmpty()) {
            for (int i = 0; i < tempUrls.size(); i++) {
                // 이미지 순서 설정 (1부터 시작)
                int order = i + 1;

                // 임시 경로에 저장된 각 이미지를 정식 경로에 이동
                moveCommunityImage(tempUrls.get(i), relatedNumber, order);
            }
            log.info("임시 이미지 정식 저장 완료 tempUrls: {}", tempUrls);
        }

        return getCommunityImages(relatedNumber);
    }

    private void deleteCommunityImage(String url) {
        Image targetImage = imageRepository.findByUrl(url)
                .orElseThrow(() -> {
                    log.warn("Image Not Found. URL: {}", url);
                    throw new IllegalArgumentException("해당 이미지를 찾을 수 없습니다. URL: " + url);
                });
        String targetImageKey = targetImage.getKey();

        // S3에서 파일 삭제
        s3Uploader.deleteFile(targetImageKey);

        // DB에서 이미지 데이터 삭제
        imageRepository.delete(targetImage);
    }

    private void moveCommunityImage(String url, int relatedNumber, int order) {
        Image image = imageRepository.findByUrl(url)
                .orElseThrow(() -> {
                    log.warn("Image Not Found. URL: {}", url);
                    throw new IllegalArgumentException("해당 이미지를 찾을 수 없습니다. URL: " + url);
                });
        String key = image.getKey();

        if (!s3Uploader.existObject(key)) {
            throw new IllegalArgumentException("저장된 데이터가 존재하지 않습니다. Url을 확인해주세요. URL: " + url);
        }

        // 새로운 S3 Key 생성
        String newKey = s3KeyHandler.generateS3Key("community", relatedNumber, image.getStorageName(), order);

        // 이미지를 새로운 경로로 이동
        s3Uploader.moveImage(key, newKey);

        // 새로운 key에 해당하는 URL
        String newUrl = s3Uploader.getImageUrl(newKey);

        // DB 이미지 데이터 수정
        image.update(relatedNumber, order, newKey, newUrl, LocalDateTime.now());
    }

    // 커뮤니티 이미지 수정
    @Transactional
    public List<ImageDetailResponseDto> updateCommunityImages(int relatedNumber, int userNumber, List<String> statuses, List<String> urls) {

        // 게시글 작성자 검증
        if (userNumber != communityRepository.findByPostNumber(relatedNumber).get().getUserNumber()) {
            log.warn("커뮤니티 게시글 수정 권한이 없습니다. communityPostNumber: {}", relatedNumber);
            throw new IllegalArgumentException("커뮤니티 게시글 수정 권한이 없습니다.");
        }

        // 각 이미지에 대해 변경 사항 적용
        int order = 1;
        for (int i = 0; i < urls.size(); i++) {
            String status = statuses.get(i);
            String url = urls.get(i);

            // 아무 변화가 없는 경우
            if (status.equals("n")) {
                order++; // 아무 동작 하지 않고 순서값 +1
            }

            // 이미지 순서 변경 or 임시저장 이미지인 경우
            else if (status.equals("y") || status.equals("i")) {
                moveCommunityImage(url, relatedNumber, order);
                order++; // 순서값 증가
                log.info("이미지 경로 수정 완료 URL: {}", url);
            }

            // 이미지가 삭제되는 경우
            else if (status.equals("d")) {
                deleteCommunityImage(url);
                log.info("이미지 삭제 완료 URL: {}", url);
            }

            // status 예외 처리
            else {
                log.warn("잘못된 커뮤니티 이미지 수정 status입니다. status: {}", status);
                throw new IllegalArgumentException("잘못된 status입니다. 가능한 status값: n(변경 없음), d(삭제), y(순서 변경), i(임시 저장)");
            }
        }

        return getCommunityImages(relatedNumber);
    }

    @Transactional
    public void deleteCommunityImage(String relatedType, int relatedNumber, int userNumber) {
        //게시글 작성자인지 검증
        if (userNumber != communityRepository.findByPostNumber(relatedNumber).get().getUserNumber()) {
        } else {
            throw new IllegalArgumentException("게시글 작성자가 아닙니다.");
        }
        imageService.deleteImage(relatedType, relatedNumber);
    }

    // 커뮤니티 게시물 이미지 조회
    public List<ImageDetailResponseDto> getCommunityImages(int postNumber) {
        List<Image> images = imageRepository.findAllByRelatedTypeAndRelatedNumber("community", postNumber);
        return images.stream()
                .map(ImageDetailResponseDto::from)
                .collect(Collectors.toList());
    }

    // 게시물 별 이미지 조회
    public ImageDetailResponseDto[] communityImageDetail(int postNumber) {
        log.info("게시물 별 이미지 조회 postNumber: {}", postNumber);

        List<Image> images = imageRepository.findAllByRelatedTypeAndRelatedNumber("community", postNumber);
        ImageDetailResponseDto[] responses = new ImageDetailResponseDto[images.size()];

        for (int i = 0; i < images.size(); i++) {
            Image image = images.get(i);
            log.info("postNumber: {}, order: {}", postNumber, image.getOrder());

            ImageDetailResponseDto imageDetail = ImageDetailResponseDto.from(image);
            responses[i] = imageDetail;
        }

        return responses;
    }

    @Transactional
    public ImageDetailResponseDto finalizeTemporaryImages(String sourceKey, ImageUpdateRequestDto updateRequest) {
        System.out.println("이미지 정식 저장된 이미지 DB업데이트 메소드 동작 : " + updateRequest.getOrder());

        Image searchImage = imageRepository.findByKey(sourceKey)
                .orElseThrow(() -> new IllegalArgumentException("정식 저장 DB update 동작 에러 : 수정할 이미지를 찾을 수 없습니다. : " + sourceKey));

        //update 메소드 호출
        searchImage.update(
                updateRequest.getOriginalName(),
                updateRequest.getStorageName(),
                updateRequest.getSize(),
                updateRequest.getFormat(),
                updateRequest.getRelatedType(),
                updateRequest.getRelatedNumber(),
                updateRequest.getOrder(),
                updateRequest.getKey(),
                updateRequest.getUrl(),
                updateRequest.getUploadDate()
        );
        Image updatedImage = imageRepository.save(searchImage); // save 호출 추가


        System.out.println("finalizeTemporaryImages 메소드 동작 : " + updatedImage.getOrder());

        //DB에 update 적용
        return new ImageDetailResponseDto(updatedImage);
    }

}
