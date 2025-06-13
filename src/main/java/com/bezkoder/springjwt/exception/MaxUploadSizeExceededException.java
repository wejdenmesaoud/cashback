package com.bezkoder.springjwt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
public class MaxUploadSizeExceededException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public MaxUploadSizeExceededException(String message) {
        super(message);
    }
}
