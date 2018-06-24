package ~~ROOT_PACKAGE~~.common

import groovy.util.logging.Slf4j
import ~~ROOT_PACKAGE~~.common.aspect.LoggingTelemetryAspect
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError


/**
 * This is an element in our standard error response object an item in a
 * JsonApiErrorReponse.
 *
 * If ReST API methods return a response body, it should match the Content-Type.
 *
 * title is the only required member.
 *
 * for more details about members see http://jsonapi.org/format/#errors
 *
 * NOTE: without this object, spring will return failures like this when required params are not provided:
 * {
 *    "timestamp": 1513619231464,
 *    "status": 400,
 *    "error": "Bad Request",
 *    "exception": "org.springframework.http.converter.HttpMessageNotReadableException",
 *    "message": "Required request body is missing: public org.springframework.http.ResponseEntity<io.ctl.platform.cloudStarterJava.model.Contact> io.ctl.platform.cloudStarterJava.controller.ContactsController.createContact(io.ctl.platform.cloudStarterJava.model.Contact)",
 *    "path": "/contacts"
 * }
 */
@Slf4j
class JsonApiError {

    // a unique identifier for this particular occurrence of the problem - logguid
    private String id

    // a links object containing the following members:
    //   about: a link that leads to further details about this particular
    //          occurrence of the problem.
    // private JsonLinks[] links - not currently implemented

    // the HTTP status code applicable to this problem, expressed as a string value.
    private String status

    // an application-specific error code, expressed as a string value.
    private String code

    // a short, human-readable summary of the problem that SHOULD NOT change from
    // occurrence to occurrence of the problem, except for purposes of localization.
    private String title

    // a human-readable explanation specific to this occurrence of the problem.
    // Like title, this field’s value can be localized.
    private String detail

    // an object containing references to the source of the error, optionally including
    // any of the following members:
    //   pointer: a JSON Pointer [RFC6901] to the associated entity in the request document
    //            [e.g. '/data' for a primary data object, or '/data/attributes/title'
    //            for a specific attribute].
    //   parameter: a string indicating which URI query parameter caused the error.
    // private JsonSource source - not currently implemented

    // a meta object containing non-standard meta-information about the error.
    private Meta meta

    JsonApiError(String title) {
        this.title = title
        this.id = MDC.get(LoggingTelemetryAspect.X_REQUEST_ID_KEY)
    }

    JsonApiError(HttpStatus statusCode) {
        this(statusCode.getReasonPhrase())
        this.status = statusCode.toString()
    }

    JsonApiError(HttpStatus statusCode, String detail) {
        this(statusCode)
        this.detail = detail
    }

    JsonApiError(HttpStatus statusCode, BindingResult br) {
        this(statusCode)
        this.detail = 'Your request had validation errors'
        this.meta = new Meta(br)
    }

    JsonApiError(HttpStatus statusCode, List<String> errors) {
        this(statusCode)
        this.detail = 'You request had validation errors'
        this.meta = new Meta(errors)
    }

    class Meta {
        private List<String> validationErrors

        Meta(List<String> validationErrors) {
            this.validationErrors = validationErrors
        }

        // Construct from Validation Constraint violations
        Meta(BindingResult br) {
            validationErrors = new LinkedList<>()
            for(FieldError fe : br.getFieldErrors()) {
                validationErrors.add(extractFieldError(fe))
            }
        }

        private String extractFieldError(FieldError fe) {
            // e.g. Field instanceType: an instance type must be provided. You provided 'null'
            StringBuilder sb = new StringBuilder('Field ')
            sb.append(fe.getField())
                    .append(': ')
                    .append(fe.getDefaultMessage())
                    .append('. You provided \'')
                    .append(fe.getRejectedValue())
                    .append('\'')
            return sb.toString()
        }
    }
}
