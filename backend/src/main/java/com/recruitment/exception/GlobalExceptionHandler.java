package com.recruitment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> validation(MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("error","Validation failed"));}
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String,String>> upload(MaxUploadSizeExceededException e){return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of("error","Uploaded file is too large"));}
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> responseStatus(ResponseStatusException e) {
        String reason=e.getReason()==null?"Request could not be completed":e.getReason();
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("error",reason));
    }
}
