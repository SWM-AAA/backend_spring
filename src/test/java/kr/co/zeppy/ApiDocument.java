package kr.co.zeppy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;

@EnableMockMvc
@AutoConfigureRestDocs
public class ApiDocument {
    
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // 결과를 출력하고, 문서 스니펫을 만드는 메서드
    protected void printAndMakeSnippet(ResultActions resultActions, String title) throws Exception {
        MvcResult mvcResult = resultActions.andReturn();
        System.err.println("mvcResult Output: " + mvcResult.getResponse().getContentAsString());
        resultActions.andDo(print())
                .andDo(toDocument(title));
    }


    // 객체를 JSON 문자열로 직렬화하는 메서드
    protected String toJson(Object object) {
        try {
            String jsonString = objectMapper.writeValueAsString(object);
            System.out.println("JSON Output: " + jsonString);
            return jsonString;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Seriallizer Error", e);
        }
    }

    private RestDocumentationResultHandler toDocument(String title) {
        return MockMvcRestDocumentationWrapper.document(title, getDocumentRequest(), getDocumentResponse());
    }

    // 문서에 표시될 요청의 형태를 정의하는 메서드
    private OperationRequestPreprocessor getDocumentRequest() {
        return preprocessRequest(
                modifyUris()
                        .scheme("https")
                        .host("zeppy.co.kr")
                        .removePort(),
                prettyPrint());
    }

    // 문서에 표시될 응답의 형태를 정의하는 메서드
    private OperationResponsePreprocessor getDocumentResponse() {
        return preprocessResponse(prettyPrint());
    }
}