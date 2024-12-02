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
                    .forEach(deletedTempUrl -> deleteTempCommunityImage(deletedTempUrl));
            log.info("임시 이미지 삭제 완료 deletedTempUrls: {}", deletedTempUrls);
        }

        // 정식 등록: 커뮤니티 게시글에 최종 포함된 이미지 처리
        if (tempUrls != null && !tempUrls.isEmpty()) {
            for (int i = 0; i < tempUrls.size(); i++) {
                // 이미지 순서 설정 (1부터 시작)
                int order = i + 1;

                // 임시 경로에 저장된 각 이미지를 정식 저장
                saveOfficialCommunityImage(tempUrls.get(i), relatedNumber, order);
            }
            log.info("임시 이미지 정식 저장 완료 tempUrls: {}", tempUrls);
        }

        return getCommunityImages(relatedNumber);
    }

    private void deleteTempCommunityImage(String deletedTempUrl) {
        Image deletedTempImage = imageRepository.findByUrl(deletedTempUrl)
                .orElseThrow(() -> {
                    log.warn("Image Not Found. URL: {}", deletedTempUrl);
                    throw new IllegalArgumentException("해당 이미지를 찾을 수 없습니다. URL: " + deletedTempUrl);
                });
        String deletedTempKey = deletedTempImage.getKey();

        // S3에서 임시 저장한 파일 삭제
        s3Uploader.deleteFile(deletedTempKey);

        // DB에서 이미지 데이터 삭제
        imageRepository.delete(deletedTempImage);
    }

    public void saveOfficialCommunityImage(String tempUrl, int relatedNumber, int order) {
        Image tempImage = imageRepository.findByUrl(tempUrl)
                .orElseThrow(() -> {
                    log.warn("Image Not Found. URL: {}", tempUrl);
                    throw new IllegalArgumentException("해당 이미지를 찾을 수 없습니다. URL: " + tempUrl);
                });
        String tempKey = tempImage.getKey();

        if (!s3Uploader.existObject(tempKey)) {
            throw new IllegalArgumentException("임시 저장된 데이터가 존재하지 않습니다. Url을 확인해주세요. URL: " + tempUrl);
        }

        // 정식 저장을 위한 Key 생성
        String newKey = s3KeyHandler.generateS3Key("community", relatedNumber, tempImage.getStorageName(), order);

        // 임시 경로에 있는 이미지 정식 경로로 이동
        s3Uploader.moveImage(tempKey, newKey);

        // 정식 경로 key로 url 가져오기
        String newUrl = s3Uploader.getImageUrl(newKey);

        // DB 이미지 데이터 수정
        tempImage.update(relatedNumber, order, newKey, newUrl, LocalDateTime.now());
    }

    @Transactional
    //커뮤니티 이미지 수정
    public ImageDetailResponseDto[] updateCommunityImage(int relatedNumber, List<String> statuses, List<String> urls, int userNumber) {

        //게시글 작성자인지 검증
        if (userNumber == communityRepository.findByPostNumber(relatedNumber).get().getUserNumber()) {
        } else {
            throw new IllegalArgumentException("게시글 작성자가 아닙니다.");

        }

        String relatedType = "community";

        int order = 1;
        int index = 0;

        for (int i = 0; i < urls.size(); i++) {
            index = i;
            String status = statuses.get(index);
            String url = urls.get(index);

            if (status.equals("n")) {
                //아무 동작 하지 않고 순서값 +1
                order++;
            } else if (status.equals("y")) {
                //현재 url로 key 가져오기
                String key = s3KeyHandler.getKeyByUrl(url);
                System.out.println("key : " + key);

                //unique 한 값인 key로 db 데이터 가져오기
                Image image = imageRepository.findByKey(key)
                        .orElseThrow(() -> new IllegalArgumentException("해당 이미지를 찾을 수 없습니다."));


                //현재 순서 값에 대한 새로운 key 생성
                String newKey = s3KeyHandler.generateS3Key(relatedType, relatedNumber, image.getStorageName(), order);
                System.out.println("newKey : " + newKey);
                //새로운 key로 경로 이동
                String destinationKey = s3Uploader.moveImage(key, newKey);
                //새로운 key로 새로운 url 가져오기
                String newUrl = s3Uploader.getImageUrl(destinationKey);
                System.out.println("newUrl : " + newUrl);

                //DB update 동작
                ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
                        .relatedType("community")
                        .relatedNumber(relatedNumber)
                        .order(order)
                        .key(destinationKey)
                        .url(newUrl) // 새 이미지 URL
                        .build();
                finalizeTemporaryImages(key, updateRequest);

                //순서값 +1
                order++;

            } else if (status.equals("d")) {

                //S3 에서 이미지 삭제
                //url로 key값 가져와서 삭제 동작
                String key = s3KeyHandler.getKeyByUrl(url);
                s3Uploader.deleteFile(key);

                //DB에서 이미지 삭제
                //unique 한 값인 key로 db 데이터 가져오기
                Image image = imageRepository.findByKey(key)
                        .orElseThrow(() -> new IllegalArgumentException("해당 이미지를 찾을 수 없습니다."));
                //DB에서 삭제
                imageRepository.delete(image);

                //순서값 변동 X

            } else if (status.equals("i")) {
                //임시 저장 이미지
                String tempUrl = url;
                // 임시 경로 key 추출
                String tempKey = s3KeyHandler.getKeyByUrl(tempUrl);
                System.out.println("tempKey : " + tempKey);

                //해당 경로에 이미지가 존재하는지 확인
                if (s3Uploader.existObject(tempKey)) {

                    //unique한 값인 key로 db의 임시 이미지 데이터 가져오기
                    Image tempImage = imageRepository.findByKey(tempKey)
                            .orElseThrow(() -> new IllegalArgumentException("해당 이미지를 찾을 수 없습니다."));


                    //정식 경로 key 생성
                    String newKey = s3KeyHandler.generateS3Key(relatedType, relatedNumber, tempImage.getStorageName(), order);
                    System.out.println("newKey : " + newKey);
                    //임시 경로에 있는 이미지 정식 경로로 이동
                    s3Uploader.moveImage(tempKey, newKey);
                    //정식 경로 key로 url 가져오기
                    String newUrl = s3Uploader.getImageUrl(newKey);
                    System.out.println("newUrl : " + newUrl);

                    //DB 업데이트 동작
                    ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
                            .relatedType(relatedType)
                            .relatedNumber(relatedNumber)
                            .order(order)
                            .key(newKey)
                            .url(newUrl) // 새 이미지 URL
                            .build();
                    finalizeTemporaryImages(tempKey, updateRequest);

                    //순서값 +1
                    order++;

                } else {
                    throw new IllegalArgumentException(" 잘못된 입력입니다. status 값을 확인해주세요");

                }
            }
        }
        ImageDetailResponseDto[] responses = communityImageDetail(relatedNumber);
        return responses;
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
