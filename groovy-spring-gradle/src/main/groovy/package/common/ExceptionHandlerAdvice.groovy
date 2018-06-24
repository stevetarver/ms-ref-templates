package ~~ROOT_PACKAGE~~.common

import static net.logstash.logback.argument.StructuredArguments.kv

import groovy.util.logging.Slf4j
import ~~ROOT_PACKAGE~~.exception.ResourceNotFoundInCollectionException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException

import java.security.SignatureException

/*
 * I should catch every exception that tries to leave this project and format
 * the result as a json:api error
 *
 * I contain service specific exception handlers
 * AND a handler for every 4xx, 5xx error to provide:
 *   - consistent error responses
 *   - capturing all exceptions and eating stack traces
 *   - preventing Tomcat error pages from being shown to users
 *
 * I use a handle* method name and log the response status to simplify finding
 * errors in Kibana.
 */
@Slf4j
@ControllerAdvice
class ExceptionHandlerAdvice {

    private static final String STATUS = 'status'
    private static final String EXCEPTION_MSG = 'exceptionMsg'

    // Project Exception Handlers -------------------------------------------------------
    @ExceptionHandler
    ResponseEntity<JsonApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundInCollectionException e) {
        return exceptionResponse(HttpStatus.NOT_FOUND, e)
    }

    // Global Exception Handlers --------------------------------------------------------

    @ExceptionHandler
    ResponseEntity<JsonApiErrorResponse> handleInvalidSignature(SignatureException signatureException) {
        this.log.error('Invalid signature for request',
                kv(EXCEPTION_MSG, signatureException.getMessage()),
                kv(STATUS, HttpStatus.UNAUTHORIZED.value()))
        return exceptionResponse(HttpStatus.UNAUTHORIZED, signatureException)
    }

    // Validation constraint exception handler
    @ExceptionHandler
    ResponseEntity<JsonApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        // No logging needed, the o.s.w.s.h.AbstractHandlerExceptionResolver does it for us
        return exceptionResponse(HttpStatus.BAD_REQUEST, e.getBindingResult())
    }

    // Handle bad URIs that don't make it into a controller class
    // CONSIDER: if there are many of these internal exceptions, consider
    //           subclassing ResponseEntityExceptionHandler to handle them,
    //           perhaps handleExceptionInternal() to catch all of them
    @ExceptionHandler
    ResponseEntity<JsonApiErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException e) {
        this.log.error('No handler found exception - no rest api matches the given URI, method, and parameters',
                kv(EXCEPTION_MSG, e.getMessage()),
                kv(STATUS, HttpStatus.NOT_FOUND.value()))
        return exceptionResponse(HttpStatus.NOT_FOUND, e)
    }

    @ExceptionHandler
    ResponseEntity<JsonApiErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e) {
        this.log.error('Http media type not supported exception',
                kv(EXCEPTION_MSG, e.getMessage()),
                kv(STATUS, HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
        return exceptionResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e)
    }

    @ExceptionHandler
    ResponseEntity<JsonApiErrorResponse> handleIllegalStateException(
            IllegalStateException e) {
        this.log.error('Illegal state exception',
                kv(EXCEPTION_MSG, e.getMessage()),
                kv(STATUS, HttpStatus.CONFLICT.value()),
                e)
        return exceptionResponse(HttpStatus.CONFLICT, e)
    }

    // Failed to convert request field to method types - e.g. convert string to Status enum
    @ExceptionHandler
    ResponseEntity<JsonApiErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        this.log.error('Method argument type mismatch exception',
                kv(EXCEPTION_MSG, e.getMessage()),
                kv(STATUS, HttpStatus.BAD_REQUEST.value()))
        String msg = "Supplied value '${e.getValue()}' is not valid for field '${e.getName()}'"
        return exceptionResponse(HttpStatus.BAD_REQUEST, msg)
    }

    // Global catch-all to prevent any unhandled exception from going to the client
    // If you find one of these, we should probably have a more specific handler
    @ExceptionHandler
    ResponseEntity<JsonApiErrorResponse> handleUnhandledThrowable(
            Throwable throwable) {
        this.log.error('Unhandled Throwable exception',
                kv(EXCEPTION_MSG, throwable.getLocalizedMessage()),
                kv(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value()),
                throwable)
        return exceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getLocalizedMessage())
    }


    private ResponseEntity<JsonApiErrorResponse> exceptionResponse(
            HttpStatus status, JsonApiErrorResponse errors) {
        return new ResponseEntity<>(errors, status)
    }

    private ResponseEntity<JsonApiErrorResponse> exceptionResponse(
            HttpStatus status, Exception e) {
        return exceptionResponse(status, new JsonApiErrorResponse(new JsonApiError(status, e.getMessage())))
    }

    private ResponseEntity<JsonApiErrorResponse> exceptionResponse(
            HttpStatus status, String detail) {
        return exceptionResponse(status, new JsonApiErrorResponse(new JsonApiError(status, detail)))
    }

    private ResponseEntity<JsonApiErrorResponse> exceptionResponse(
            HttpStatus status, BindingResult br) {
        return exceptionResponse(status, new JsonApiErrorResponse(new JsonApiError(status, br)))
    }

    private ResponseEntity<JsonApiErrorResponse> exceptionResponse(HttpStatus status, List<String> errors) {
        return exceptionResponse(status, new JsonApiErrorResponse(new JsonApiError(status, errors)))
    }

}
