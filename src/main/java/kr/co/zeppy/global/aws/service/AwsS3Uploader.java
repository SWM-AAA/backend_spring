package kr.co.zeppy.global.aws.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Service
@Transactional
public class AwsS3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;

    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(IllegalArgumentException::new);

        return uploadS3(uploadFile, dirName);
    }

    private String uploadS3(File uploadFile, String dirName) {
        String fileName = dirName;
        // S3로 업로드
        String uploadImageUrl = null;
        try {
            uploadImageUrl = putS3(uploadFile, fileName);
        } finally {
            removeNewFile(uploadFile);
        }
        return uploadImageUrl;
    }

    // 1. 로컬에 파일생성
    private Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File(file.getOriginalFilename());
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }

        return Optional.empty();
    }

    // 2. S3에 파일업로드
    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        log.info("File Upload : " + fileName);
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    // 3. 로컬에 생성된 파일삭제
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("File delete success");
            return;
        }
        log.info("File delete fail");
    }


    public void deleteS3(String fileName) {
        log.info("File Delete : " + fileName);
        amazonS3Client.deleteObject(bucket, fileName);
    }
}