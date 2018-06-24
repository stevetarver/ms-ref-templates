package ~~ROOT_PACKAGE~~.common.interceptor

import static net.logstash.logback.argument.StructuredArguments.kv

import groovy.util.logging.Slf4j
import ~~ROOT_PACKAGE~~.common.aspect.LoggingTelemetryAspect
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * I extract the x-request-id header and add it to MDC so that logging can include it in each
 * log entry. If one does not exist, I add a random UUID.
 * I also make an apiRequest log entry so batman can easily recreate the call via Postman.
 * After the request is completed, I remove the x-request-id in preparation for the next call.
 *
 * NOTES:
 * - If your api has some key piece of information that you want added to every log entry,
 *   perhaps AccountAlias, you can add to / remove from MDC here and it will automatically
 *   be added to every log entry on this thread. Why? If you received a customer ticket
 *   you could easily see all log entries made for that customer account with a simple ELK
 *   query.
 */
@Slf4j
@Component
class RequestInterceptor extends HandlerInterceptorAdapter {

    @Override
    boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object o) throws Exception {
        String logguid = req.getHeader(LoggingTelemetryAspect.X_REQUEST_ID_HDR)?.trim()
        if(!logguid) {
            logguid = UUID.randomUUID().toString()
        }
        MDC.put(LoggingTelemetryAspect.X_REQUEST_ID_KEY, logguid)

        // To provide batman a way to replicate this call, we log all parameters related to the call
        // Omit healthz endpoints
        String reqUri = req.getRequestURI();
        if(null != reqUri && !reqUri.contains("healthz")) {
            log.info("{} requested", reqUri,
                    kv("logCategory", "apiRequest"),
                    kv("reqOrigin", req.getRemoteHost()),
                    kv("reqVerb", req.getMethod()),
                    kv("reqPath", reqUri),
                    kv("reqQuery", req.getQueryString()));
        }

        // TODO: this will eat the body and it will not be available to framework for further parsing
        //String reqBody = req.getReader().lines().collect(Collectors.joining(""));
        // Apparently, we have to create a filter that allows this and add it to the filter chain

        return true;
    }

    /**
     * I remove x-request-id after request processing, including exception handling.
     * Note: postHandle is not called if an exception is thrown
     */
    @Override
    void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex) throws Exception {
        MDC.remove(LoggingTelemetryAspect.X_REQUEST_ID_KEY);
    }
}
