package com.retailer.rewards.exception;

import com.retailer.rewards.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@RestControllerAdvice
public class GlobalExceptionHandler
        extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Object> handleCustomerNotFound(
            CustomerNotFoundException exception,
            WebRequest request) {

        return errorResponse(
                HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler({
            InvalidDateRangeException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<Object> handleInvalidInput(
            RuntimeException exception,
            WebRequest request) {

        return errorResponse(
                HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        return errorResponse(
                HttpStatus.BAD_REQUEST,
                "Required parameter is missing: "
                        + exception.getParameterName(),
                request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException exception,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        return errorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid parameter value. Customer ID must be a number "
                        + "and dates must use YYYY-MM-DD format.",
                request);
    }

    @ExceptionHandler({
            CompletionException.class,
            ExecutionException.class
    })
    public ResponseEntity<Object> handleAsyncFailure(
            Exception exception,
            WebRequest request) {

        Throwable cause = exception;

        while ((cause instanceof CompletionException
                || cause instanceof ExecutionException)
                && cause.getCause() != null) {
            cause = cause.getCause();
        }

        if (cause instanceof CustomerNotFoundException) {
            return errorResponse(
                    HttpStatus.NOT_FOUND, cause.getMessage(), request);
        }

        if (cause instanceof InvalidDateRangeException
                || cause instanceof IllegalArgumentException) {
            return errorResponse(
                    HttpStatus.BAD_REQUEST, cause.getMessage(), request);
        }

        return unexpectedError(cause, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(
            Exception exception,
            WebRequest request) {

        return unexpectedError(exception, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            Object body,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        ErrorResponse response = createError(
                status, status.getReasonPhrase(), request);

        return super.handleExceptionInternal(
                exception, response, headers, status, request);
    }

    private ResponseEntity<Object> unexpectedError(
            Throwable exception, WebRequest request) {

        LOGGER.error("Unexpected error while processing rewards request",
                exception);

        return errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to process the request. Please try again later.",
                request);
    }

    private ResponseEntity<Object> errorResponse(
            HttpStatus status, String message, WebRequest request) {

        return ResponseEntity.status(status)
                .body(createError(status, message, request));
    }

    private ErrorResponse createError(
            HttpStatus status, String message, WebRequest request) {

        String path = ((ServletWebRequest) request)
                .getRequest().getRequestURI();

        return new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                path);
    }
}