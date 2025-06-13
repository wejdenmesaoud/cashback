package com.bezkoder.springjwt.exception;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle AccessDeniedException (insufficient permissions)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        logger.error("Access denied: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.FORBIDDEN.value(),
            "Access Denied",
            "You do not have permission to access this resource",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    
    // Handle AuthenticationException (general authentication failures)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        logger.error("Authentication failed: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication Failed",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    // Handle BadCredentialsException (invalid username/password)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        logger.error("Bad credentials: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "Invalid Credentials",
            "The username or password you provided is incorrect",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    // Handle UsernameNotFoundException
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        logger.error("Username not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "User Not Found",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    // Handle DisabledException (account disabled)
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex, WebRequest request) {
        logger.error("Account disabled: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "Account Disabled",
            "Your account has been disabled. Please contact support.",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    // Handle LockedException (account locked)
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(LockedException ex, WebRequest request) {
        logger.error("Account locked: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "Account Locked",
            "Your account has been locked. Please contact support.",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    // Handle JWT Exceptions
    
    // Handle ExpiredJwtException
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(ExpiredJwtException ex, WebRequest request) {
        logger.error("JWT expired: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "JWT Expired",
            "Your session has expired. Please log in again.",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    // Handle UnsupportedJwtException
    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedJwtException(UnsupportedJwtException ex, WebRequest request) {
        logger.error("Unsupported JWT: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "Unsupported JWT",
            "The provided JWT token format is not supported",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    // Handle MalformedJwtException
    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJwtException(MalformedJwtException ex, WebRequest request) {
        logger.error("Malformed JWT: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "Malformed JWT",
            "The provided JWT token is malformed",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    // Handle IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.error("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Request",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    // Handle ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        logger.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.NOT_FOUND.value(),
            "Resource Not Found",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    // Handle DuplicateResourceException
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex, WebRequest request) {
        logger.error("Duplicate resource: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.CONFLICT.value(),
            "Duplicate Resource",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    // Handle InvalidDataException
    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDataException(InvalidDataException ex, WebRequest request) {
        logger.error("Invalid data: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Data",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    // Handle OperationNotAllowedException
    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleOperationNotAllowedException(OperationNotAllowedException ex, WebRequest request) {
        logger.error("Operation not allowed: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.FORBIDDEN.value(),
            "Operation Not Allowed",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    
    // Handle TokenRefreshException
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorResponse> handleTokenRefreshException(TokenRefreshException ex, WebRequest request) {
        logger.error("Token refresh error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.FORBIDDEN.value(),
            "Token Refresh Error",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    
    // Handle MaxUploadSizeExceededException
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, WebRequest request) {
        logger.error("File size exceeded: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.PAYLOAD_TOO_LARGE.value(),
            "File Size Exceeded",
            "The uploaded file exceeds the maximum allowed size",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }
    
    // Handle EntityNotFoundException
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        logger.error("Entity not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.NOT_FOUND.value(),
            "Entity Not Found",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    // Handle ConstraintViolationException
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        logger.error("Constraint violation: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    // Handle DataIntegrityViolationException
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.CONFLICT.value(),
            "Data Integrity Violation",
            "A data integrity constraint was violated",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    // Handle InsufficientAuthenticationException
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientAuthenticationException(InsufficientAuthenticationException ex, WebRequest request) {
        logger.error("Insufficient authentication: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication Required",
            "Full authentication is required to access this resource",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Server Error",
            "An unexpected error occurred",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // Override methods from ResponseEntityExceptionHandler
    
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, 
            HttpHeaders headers, 
            HttpStatusCode status, 
            WebRequest request) {
        
        logger.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            new Date(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            "There were validation errors in your request",
            request.getDescription(false),
            errors
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        logger.error("Method not supported: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            "Method Not Allowed",
            ex.getMessage(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }
    
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        logger.error("Media type not supported: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            "Unsupported Media Type",
            ex.getMessage(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
    
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        logger.error("Missing parameter: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.BAD_REQUEST.value(),
            "Missing Parameter",
            ex.getMessage(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        logger.error("Message not readable: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.BAD_REQUEST.value(),
            "Malformed JSON Request",
            "The request body could not be read as valid JSON",
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        logger.error("No handler found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.NOT_FOUND.value(),
            "Resource Not Found",
            String.format("Could not find the %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL()),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
