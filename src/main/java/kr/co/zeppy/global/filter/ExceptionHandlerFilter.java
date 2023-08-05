package kr.co.zeppy.global.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.zeppy.global.dto.ErrorResponse;
import kr.co.zeppy.global.error.ExpiredJwtException;
import kr.co.zeppy.global.error.InvalidJwtException;
import kr.co.zeppy.global.error.ZeppyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    
    private static final String UTF_8 = "utf-8";
    private static final String APPLICATION_JSON = "application/json";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                                    throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (InvalidJwtException | ExpiredJwtException e) {
            setErrorResponse(response, e);
        }
    }

    private void setErrorResponse(HttpServletResponse response, ZeppyException zeppyException) {
        response.setStatus(zeppyException.getStatus().value());
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF_8);
        try {
            response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(zeppyException)));
        } catch (IOException e) {
            log.error("Error Response Write Exception", e);
        }
    }
}
