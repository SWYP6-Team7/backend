package swyp.swyp6_team7.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.image.domain.Image;
import swyp.swyp6_team7.image.dto.request.ImageCreateRequestDto;
import swyp.swyp6_team7.image.dto.request.ImageUpdateRequestDto;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.image.s3.S3Uploader;
import swyp.swyp6_team7.image.util.S3KeyHandler;
import swyp.swyp6_team7.image.util.StorageNameHandler;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ImageCommunityService {

    private final ImageRepository imageRepository;
    private final S3Uploader s3Uploader;
    private final ImageService imageService;
    private final StorageNameHandler storageNameHandler;
    private final S3KeyHandler s3KeyHandler;
    private final CommunityRepository communityRepository;


    //커뮤니티 이미지 임시 저장
    @Transactional
    public ImageDetailResponseDto uploadTempImage(MultipartFile file) throws IOException {

        String relatedType = "community";

        //임시 경로에 이미지 업로드
        String key = s3Uploader.uploadInTemporary(file, relatedType);
        String savedImageUrl = s3Uploader.getImageUrl(key);


        // 메타 데이터 뽑아서 create dto에 담기
        ImageCreateRequestDto imageTempCreateDto = ImageCreateRequestDto.builder()
                .originalName(file.getOriginalFilename())
                .storageName(storageNameHandler.generateUniqueFileName(file.getOriginalFilename()))
                .size(file.getSize())
                .format(file.getContentType())
                .relatedType(relatedType)
                .key(key)
                .url(savedImageUrl)
                .build();

        // DB에 저장
        Image image = imageTempCreateDto.toImageEntity();

        Image uploadedImage = imageRepository.save(image);
        return new ImageDetailResponseDto(uploadedImage);
    }


    //커뮤니티 이미지 정식 등록
    @Transactional
    public ImageDetailResponseDto[] saveCommunityImage(int relatedNumber, List<String> deletedTempUrls, List<String> tempUrls) {
        String relatedType = "community";
        int order = 0;


        //임시 저장 했지만 최종 게시물 등록시에는 삭제된 이미지 처리
        for (int i = 0; i < deletedTempUrls.size(); i++) {
            String deletedTempUrl = deletedTempUrls.get(i);
            System.out.println("deletedTempUrl : " + deletedTempUrl);

            // deletedTempKey 찾기
            String deletedTempKey = imageRepository.findByUrl(deletedTempUrl)
                    .map(Image::getKey)
                    .orElseThrow(() -> new IllegalArgumentException("해당 이미지의 키를 찾을 수 없습니다."));
            System.out.println("deletedTempKey : " + deletedTempKey);

            // DB에서 이미지 삭제
            Image deletedTempImage = imageRepository.findByKey(deletedTempKey)
                    .orElseThrow(() -> new IllegalArgumentException("해당 이미지를 찾을 수 없습니다."));

            try {
                imageRepository.delete(deletedTempImage);
                System.out.println("파일 삭제 완료: " + deletedTempKey);
            } catch (DataAccessException e) {
                throw new IllegalArgumentException("이미지 삭제에 실패했습니다: " + e.getMessage());
            }

            // S3에서 삭제
            if (s3Uploader.existObject(deletedTempKey)) {
                s3Uploader.deleteFile(deletedTempKey);
            } else {
                throw new IllegalArgumentException("유효하지 않은 URL 입니다 : deletedTempUrl 을 확인해주세요 :" + deletedTempUrl);
            }
        }

        // 정식등록 할 이미지 처리
        for (int i = 0; i < tempUrls.size(); i++) {

            // 이미지 순서 설정
            order = i + 1;

            // 임시 경로에 저장된 이미지의 url 하나씩 뽑아서
            String tempUrl = tempUrls.get(i);
            System.out.println("tempUrl" + tempUrl);
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

                ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
                        .relatedType(relatedType)
                        .relatedNumber(relatedNumber)
                        .order(order)
                        .key(newKey)
                        .url(newUrl) // 새 이미지 URL
                        .build();

            } else {
                throw new IllegalArgumentException("임시 저장된 데이터가 존재하지 않습니다. Url을 확인해주세요.");
            }
        }
        ImageDetailResponseDto[] responses = communityImageDetail(relatedNumber);
        return responses;
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

            } else {
                throw new IllegalArgumentException(" 잘못된 입력입니다. status 값을 확인해주세요");
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

    //게시물 별 이미지 조회
    public ImageDetailResponseDto[] communityImageDetail(int postNumber) {
        ImageDetailResponseDto[] responses = {};

        List<Image> images = imageRepository.findAllByRelatedTypeAndRelatedNumber("community", postNumber);
        for (int i = 0; i < images.size(); i++) {

            Image image = images.get(i);

            ImageDetailResponseDto response = imageService.getImageDetailByNumber("community", postNumber, image.getOrder());
            responses[i] = response;
        }
        return responses;
    }

    @Transactional
    //url로 이미지 삭제 (= 임시저장 삭제)
    public void deleteByUrl(String url) {
        Optional<Image> image = imageRepository.findByUrl(url);

        // S3에서 파일 삭제
//        s3Uploader.deleteFile(image.get().getKey()); // 이미지의 경로를 사용하여 S3에서 삭제

        // 데이터베이스에서 이미지 삭제
        imageRepository.delete(image.get());

    }

    public ImageDetailResponseDto finalizeTemporaryImages(String sourceKey, ImageUpdateRequestDto updateRequest) {
        Optional<Image> searchImage = imageRepository.findByKey(sourceKey);
        Image image = searchImage.get();
        System.out.println(image);

        if (searchImage.isPresent()) {
            //update 메소드 호출
            image.update(
                    updateRequest.getOriginalName(),
                    updateRequest.getStorageName(),
                    updateRequest.getSize(),
                    updateRequest.getFormat(),
                    updateRequest.getRelatedType(),
                    updateRequest.getRelatedNumber(),
                    updateRequest.getOrder(),
                    updateRequest.getKey(),
                    updateRequest.getUrl(),
                    updateRequest.getUploadDate() // 현재 시간으로 업로드 날짜 설정
            );

            //DB에 update 적용
            Image updatedImage = imageRepository.save(image);
            return new ImageDetailResponseDto(updatedImage);
        } else {
            throw new IllegalArgumentException("존재하지 않는 이미지입니다.");


        }
    }

}
