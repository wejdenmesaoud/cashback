package com.bezkoder.springjwt.security.jwt;

import java.io.IOException;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.bezkoder.springjwt.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

  private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException, ServletException {
    logger.error("Unauthorized error: {}", authException.getMessage());

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    ErrorResponse errorResponse = new ErrorResponse(
        new Date(),
        HttpServletResponse.SC_UNAUTHORIZED,
        "Unauthorized",
        authException.getMessage(),
        request.getServletPath()
    );

    final ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getOutputStream(), errorResponse);
  }

}
