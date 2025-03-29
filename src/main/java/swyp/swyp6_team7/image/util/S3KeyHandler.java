package swyp.swyp6_team7.image.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import swyp.swyp6_team7.image.s3.FileFolder;
import swyp.swyp6_team7.image.s3.S3Component;


@Slf4j
@Component
public class S3KeyHandler {

    private final String baseFolder;
    private final String s3UrlPrefix;

    public S3KeyHandler(S3Component s3Component, @Value("${cloud.aws.region.static}") String s3Region) {
        this.baseFolder = s3Component.getBaseFolder();
        this.s3UrlPrefix = "https://" + s3Component.getBucket() + ".s3." + s3Region + ".amazonaws.com/";
    }


    // key 생성 메소드: {baseFolder}/{relatedType}/{relatedNumber}/{file_name}
    // relatedNumber: relatedType이 profile일때 relatedNumber는 userNumber
    public String generateS3Key(String relatedType, int relatedNumber, String storageName, int order) {

        FileFolder folderType = FileFolder.from(relatedType);

        // profile인 경우
        if (folderType == FileFolder.PROFILE) {
            return baseFolder + folderType.name().toLowerCase() + "/" + relatedNumber + "/" + storageName;
        }

        // 커뮤니티인 경우
        else if (folderType == FileFolder.COMMUNITY) {
            return baseFolder + folderType.name().toLowerCase() + "/" + relatedNumber + "/" + (order > 0 ? order + "/" : "") + storageName;
        }

        // 유효하지 않은 relatedType일 경우 예외처리
        else {
            throw new IllegalArgumentException("커뮤니티 게시물이 유효하지 않는 타입입니다.: " + relatedType);
        }
    }


    // 임시저장 key 생성 메소드: {baseFolder}/{relatedType}/{temporary}/{file_name}
    public String generateTempS3Key(String relatedType, String storageName) {
        try {
            FileFolder folderType = FileFolder.from(relatedType);
            return baseFolder + folderType.name().toLowerCase() + "/" + "temporary" + "/" + storageName;
        } catch (Exception e) {
            log.warn("유효하지 않는 타입입니다. relatedType: {}", relatedType);
            throw new IllegalArgumentException("유효하지 않는 타입입니다: " + relatedType);
        }
    }


    //url로 key를 추출하는 메소드
    public String getKeyByUrl(String url) {
        log.info("URL에서 S3 key 추출. url: {}", url);

        // URL이 올바른 형식인지 확인
        if (!url.startsWith(s3UrlPrefix)) {
            log.warn("옳지 않은 형식의 URL. url: {}", url);
            throw new IllegalArgumentException("URL 형식이 올바르지 않습니다. S3 URL인지 확인해주세요.");
        }

        return url.replace(s3UrlPrefix, "");
    }

    public String getDefaultProfileImageUrl(String defaultProfileImageName) {
        if (!defaultProfileImageName.contains("defaultProfile")) {
            throw new IllegalArgumentException("잘못된 default profile name 입니다.");
        }
        return s3UrlPrefix + baseFolder + "profile/default/" + defaultProfileImageName;
    }

    public boolean isFileUploadProfileImage(String s3Key, int relatedNumber) {
        // 파일 업로드: key가 "{baseFolder}/profile/{relatedNumber}"로 시작하는 경우
        if (s3Key.startsWith(baseFolder + "profile" + "/" + relatedNumber)) {
            return true;
        }
        return false;
    }

}
