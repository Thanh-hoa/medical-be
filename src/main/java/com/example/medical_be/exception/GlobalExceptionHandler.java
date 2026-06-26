package com.example.medical_be.exception;

import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.i18n.IMessageTranslator;
import jakarta.validation.UnexpectedTypeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException; 
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.Objects;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final IMessageTranslator iMessageTranslator;

    @Value("${app.debug:false}")
    private Boolean appDebug;

    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception e) {
        logException(e);
        String message = !appDebug
                ? iMessageTranslator.getMessage("system.crash")
                : e.getMessage();
        return processException(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApplicationException.class)
    public Object handleApplicationException(ApplicationException e) {
        logException(e);
        return processException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public Object handleIllegalStateException(IllegalStateException e) {
        logException(e);
        String message = !appDebug
                ? iMessageTranslator.getMessage("system.crash")
                : e.getMessage();
        return processException(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDeniedException(AccessDeniedException e) {
        logException(e);
        return processException(iMessageTranslator.getMessage("account.forbidden"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFoundException(NoResourceFoundException e) {
        log.error("NoResourceFoundException: {}", e.getMessage());
        return processException(iMessageTranslator.getMessage("system.no_resource_found"), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(UnexpectedTypeException.class)
    public Object handleUnexpectedTypeException(UnexpectedTypeException e) {
        logException(e);
        String message = !appDebug
                ? iMessageTranslator.getMessage("system.crash")
                : e.getMessage();
        return processException(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationException(MethodArgumentNotValidException e) {
        logException(e);
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(iMessageTranslator.getMessage("system.crash"));
        return processException(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logException(e);
        String message = !appDebug
                ? iMessageTranslator.getMessage("request.invalid_format")
                : "Invalid request format: " + e.getMostSpecificCause().getMessage();
        return processException(message, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<?> processException(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(
                JSONResponse.builder()
                        .isError(true)
                        .message(message)
                        .build());
    }

    private void logException(Exception e) {
        log.error("Exception: {}", e.getClass().getSimpleName());
        if (appDebug) {
            log.error("Exception message: {}", Objects.toString(e.getMessage(), "No message"), e);
        } else {
            log.error("Exception message: {}", Objects.toString(e.getMessage(), "No message"));
        }
    }
}
