package es.upm.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@Log4j2
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // Log de la URI y método HTTP
        log.info("Request: {} {}", request.getMethod(), request.getRequestURI());

        // Log de todos los headers
        log.info("Headers:");
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            String headerValue = request.getHeader(headerName);
            log.info("  {}: {}", headerName, headerValue);
        });

        // Log de todos los parámetros
        log.info("Parameters:");
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            log.info("  {}: {}", paramName, paramValue);
        }
        filterChain.doFilter(request, response);
    }
}

