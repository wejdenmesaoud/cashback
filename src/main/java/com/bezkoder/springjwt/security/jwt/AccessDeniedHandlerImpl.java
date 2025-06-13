package com.bezkoder.springjwt.security.jwt;

import java.io.IOException;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.bezkoder.springjwt.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(AccessDeniedHandlerImpl.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        
        logger.error("Access denied error: {}", accessDeniedException.getMessage());
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpServletResponse.SC_FORBIDDEN,
            "Access Denied",
            "You do not have permission to access this resource",
            request.getServletPath()
        );
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
