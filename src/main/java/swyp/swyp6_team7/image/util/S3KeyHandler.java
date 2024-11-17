package swyp.swyp6_team7.image.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import swyp.swyp6_team7.image.s3.FileFolder;
import swyp.swyp6_team7.image.s3.S3Component;


@Component
public class S3KeyHandler {
    private final String baseFolder;
    private final S3Component s3Component;


    @Autowired
    public S3KeyHandler(S3Component s3Component) {
        //베이스 폴더 가져오기
        this.baseFolder = s3Component.getBaseFolder();
        //relatedType이 profile일때 relatedNumber는 userNumber
        this.s3Component = s3Component;
    }

    // 동적으로 key 생성 메소드 {baseFolder}/{rleatedType}/{id}/{file_name}
    public String generateS3Key(String relatedType, int relatedNumber, String storageName, int order) {
        FileFolder folderType = FileFolder.from(relatedType);

        //profile 인 경우
        if (folderType == FileFolder.PROFILE) {
                return baseFolder + folderType.name().toLowerCase() + "/" + relatedNumber + "/" + storageName;
        }

        //커뮤니티 인경우
        else if (folderType == FileFolder.COMMUNITY) {
                return baseFolder + folderType.name().toLowerCase() + "/" + relatedNumber + "/" + (order > 0 ? order + "/" : "") + storageName;
        }

        //유효하지 않은 relatedType일 경우 예외처리
        else {
            throw new IllegalArgumentException("커뮤니티 게시물이 유효하지 않는 타입입니다.: " + relatedType);
        }
    }

    //임시저장 key 생성 메소드 {baseFolder}/{relatedType}/{temparary}/{file_name}
    public String generateTempS3Key(String relatedType, String storageName) {

        FileFolder folderType = FileFolder.from(relatedType);

        //profile 인 경우
        if (folderType == FileFolder.PROFILE) {
            return baseFolder + folderType.name().toLowerCase() + "/" + "temporary" + "/" + storageName;
        }

        //커뮤니티 인경우
        else if (folderType == FileFolder.COMMUNITY) {
            return baseFolder + folderType.name().toLowerCase() + "/" + "temporary" + "/" + storageName;
        }

        //유효하지 않은 relatedType일 경우 예외처리
        else {
            throw new IllegalArgumentException("커뮤니티 게시물이 유효하지 않는 타입입니다.: " + relatedType);
        }
    }



    //path 를 찾는 메소드
    public String getPath(String relatedType, int relatedNumber, int order) {

        FileFolder folderType = FileFolder.from(relatedType);

        //profile 인 경우
        if (folderType == FileFolder.PROFILE) {
            return baseFolder + folderType.name().toLowerCase() + "/" + relatedNumber + "/";
        }

        //커뮤니티 인경우
        else if (folderType == FileFolder.COMMUNITY) {
            return baseFolder + folderType.name().toLowerCase() + "/" + relatedNumber + "/" + (order > 0 ? order + "/" : "");
        }

        //유효하지 않은 relatedType일 경우 예외처리
        else {
            throw new IllegalArgumentException("커뮤니티 게시물이 유효하지 않는 타입입니다.: " + relatedType);
        }
    }


    //url로 key를 추출하는 메소드
    public String getKeyByUrl(String url) {
        System.out.println("S3 key로 url 추출 메소드 동작, url : " + url);
        String bucketName = s3Component.getBucket();
        String region = "ap-northeast-2";
        String S3_URL_PREFIX = "https://"+ bucketName +".s3." + region + ".amazonaws.com/";
        System.out.println("S3 URL Prefix: " + S3_URL_PREFIX);

        // URL이 올바른 형식인지 확인
        if (!url.startsWith(S3_URL_PREFIX)) {
            throw new IllegalArgumentException("URL 형식이 올바르지 않습니다. S3 URL인지 확인해주세요");
        }
        String key = url.replace(S3_URL_PREFIX, "");


        return key;
    }
}
