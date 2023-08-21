package kr.co.zeppy.global.aws.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import io.findify.s3mock.S3Mock;
import kr.co.zeppy.global.configuration.AwsS3MockConfig;

@Import(AwsS3MockConfig.class)
@SpringBootTest
class AwsS3UploaderTest {

    private static final String USER_PROFILE_IMAGE_PATH = "user/profile-image/";
    private static final String CONTENTTYPE = "image/png";
    private static final String USER_ID = "1";
    private static final String FILE_NAME = "test";

    @Autowired
    private S3Mock s3Mock;
    @Autowired
    private AwsS3Uploader awsS3Uploader;

    @AfterEach
    public void tearDown() {
        s3Mock.stop();
    }

    @Test
    void upload() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile("file", 
                    FILE_NAME, CONTENTTYPE, "test".getBytes());

        // when
        String urlPath = awsS3Uploader.upload(file, USER_PROFILE_IMAGE_PATH + USER_ID);

        // then
        assertThat(urlPath).contains(USER_PROFILE_IMAGE_PATH + USER_ID);
        assertThat(urlPath).contains(FILE_NAME);
    }
}