package kr.co.zeppy.global.aws.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.amazonaws.services.s3.AmazonS3Client;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;

import io.findify.s3mock.S3Mock;
import kr.co.zeppy.global.configuration.AwsS3MockConfig;

@Import(AwsS3MockConfig.class)
@ExtendWith(MockitoExtension.class)
class AwsS3UploaderTest {

    private static final String USER_PROFILE_IMAGE_PATH = "user/profile-image/";
    private static final String CONTENTTYPE = "image/png";
    private static final String USER_ID = "1";
    private static final String FILE_NAME = "test";
    private static final String BUCKET_NAME = "testBucketName"; // 이 부분 추가

    @InjectMocks
    private AwsS3Uploader awsS3Uploader;
    @Mock
    private S3Mock s3Mock;
    @Mock
    private AmazonS3Client amazonS3Client;

    @AfterEach
    public void tearDown() {
        s3Mock.stop();
    }

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(awsS3Uploader, "bucket", BUCKET_NAME);  // 버킷 이름 설정
    }

    @Test
    void upload() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile("file", 
                    FILE_NAME, CONTENTTYPE, "test".getBytes());

        // Mocking AmazonS3Client behavior
        when(amazonS3Client.getUrl(eq("testBucketName"), eq(USER_PROFILE_IMAGE_PATH + USER_ID)))
            .thenReturn(new URL("http://mocked-url.com/" + USER_PROFILE_IMAGE_PATH + USER_ID));

        // when
        String urlPath = awsS3Uploader.upload(file, USER_PROFILE_IMAGE_PATH + USER_ID);

        // then
        assertThat(urlPath)
                .contains(USER_PROFILE_IMAGE_PATH + USER_ID);
    }
}
