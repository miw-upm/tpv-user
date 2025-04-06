package es.upm.api.configurations;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@Log4j2
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Profile({"dev"})
public class LoggingFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("-------------------------------------------------------------------------------------------------");
        log.info("Request: {} {}", request.getMethod(), request.getRequestURI());
        log.info("Headers:");
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            String headerValue = request.getHeader(headerName);
            log.info("  {}: {}", headerName, headerValue);
        });
        log.info("Parameters:");
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            log.info("  {}: {}", paramName, paramValue);
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        byte[] requestArray = wrappedRequest.getContentAsByteArray();
        if (requestArray.length > 0) {
            String requestBody = new String(requestArray, wrappedRequest.getCharacterEncoding());
            log.info("Request body (JSON): {}", requestBody);
        }


        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(request, wrappedResponse);
        } catch (Exception e) {
            log.error("Error during filter processing", e);
            throw e;
        }

        log.info("-   -   -   -   -   -   -   -   -   -   -   -   -   -   -");
        byte[] responseArray = wrappedResponse.getContentAsByteArray();
        if (responseArray.length > 0) {
            String responseBody = new String(responseArray, response.getCharacterEncoding());
            log.info("Response body: {}", responseBody);
        }
        wrappedResponse.copyBodyToResponse();
    }
}

