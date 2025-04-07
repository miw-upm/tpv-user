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
        log.debug("-------------------------------------------------------------------------------------------------");
        log.debug("Request: {} {}", request.getMethod(), request.getRequestURI());
        log.debug("Headers:");
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            String headerValue = request.getHeader(headerName);
            log.debug("  {}: {}", headerName, headerValue);
        });
        log.debug("Parameters:");
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            log.debug("  {}: {}", paramName, paramValue);
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception e) {
            log.error("Error during filter processing", e);
            throw e;
        }

        // Ahora que se ha procesado la request, se puede obtener el cuerpo
        byte[] requestArray = wrappedRequest.getContentAsByteArray();
        if (requestArray.length > 0) {
            String requestBody = new String(requestArray, wrappedRequest.getCharacterEncoding());
            log.debug("Request body (JSON): {}", requestBody);
        }

        log.debug("-   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -    -   -   -   -");
        byte[] responseArray = wrappedResponse.getContentAsByteArray();
        if (responseArray.length > 0) {
            String responseBody = new String(responseArray, response.getCharacterEncoding());
            log.debug("Response body: {}", responseBody);
        }
        wrappedResponse.copyBodyToResponse();
    }
}

