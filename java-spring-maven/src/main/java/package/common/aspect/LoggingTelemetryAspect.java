package ~~ROOT_PACKAGE~~.common.aspect;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArgument;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

/**
 * This aspect wraps each @LoggedApi ReST API call and makes a log entry for:
 * - request: translated method name and arguments
 * - response: status and duration
 * - exception: status and duration
 *
 * NOTE: The RequestInterceptor makes the apiRequest log entry used to replicate
 *       calls using Postman.
 */
@Slf4j
@Aspect
@Component
public class LoggingTelemetryAspect {

    public static final String X_REQUEST_ID_HDR = "x-request-id";
    public static final String X_REQUEST_ID_KEY = "logRequestId";

    // Add API parameters that you want excluded from logging to this list (sensitive information)
    private static final List<String> EXCLUDED_PARAMS = Arrays.asList("authentication");
    private static final String LOG_CATEGORY_KEY = "logCategory";
    private static final String LOGGED_API_NAME_KEY = "loggedApiName";
    private static final String DURATION_KEY = "reqDurationMillis";

    // @LoggedApi on a class means all public methods
    // @LoggedApi on a method means just that method
    @Pointcut("within(@~~ROOT_PACKAGE~~.common.aspect.LoggedApi *)")
    public void loggedApiClass() {}

    @Pointcut("@annotation(~~ROOT_PACKAGE~~.common.aspect.LoggedApi)")
    public void loggedApiMethod() {}

    @Pointcut("execution(public * *(..))")
    public void publicExecution() {}

    @Pointcut("execution(* *(..))")
    public void anyExecution() {}

    @Around("(loggedApiClass() && publicExecution()) || (loggedApiMethod() && anyExecution())")
    public Object logApi(ProceedingJoinPoint joinPoint) throws Throwable {

        // Log the java call details for local testing. This is different than the
        // black box apiRequest log entry made in the RequestInterceptor which is
        // suitable for Postman invocation; this call supports replicating the call
        // in an IDE debugger.
        final String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();
        // NOTE: do not add args to this call, the presence of this vararg will prevent
        // the structured args that are added from being logged
        log.info(methodName + " request", extractParameters(methodName, joinPoint));

        // Note: Unlike golang, python, and nodejs, java has no good way to get microseconds
        //       so we use millis
        final long start = System.currentTimeMillis();
        try {
            Object response = joinPoint.proceed(joinPoint.getArgs());

            // Note: if you do not return ResponseEntity, you will not get status codes
            int status = 0;
            if(response instanceof ResponseEntity) {
                status = ((ResponseEntity)response).getStatusCode().value();
            }
            log.info("{} response", methodName,
                    keyValue(LOGGED_API_NAME_KEY, methodName),
                    keyValue(LOG_CATEGORY_KEY, "apiResponse"),
                    keyValue(DURATION_KEY, System.currentTimeMillis() - start),
                    keyValue("reqStatusCode", status));

            return response;

        } catch (Throwable throwable) {
            log.info("{} exception: {}", methodName, throwable.toString(),
                    keyValue(LOGGED_API_NAME_KEY, methodName),
                    keyValue(LOG_CATEGORY_KEY, "apiException"),
                    keyValue(DURATION_KEY, System.currentTimeMillis() - start));
            throw throwable;
        }
    }

    /**
     * Helper method for extracting request parameters.
     * Returns a StructuredArgument[] that can be used as a vararg.
     * NOTE: This method extracts translated ReST API call details - different than the apiRequest log entry
     * NOTE: We have to collect all kv pairs here because varargs will expand
     *       an array of type, but you cannot have any other args in the log.info() call.
     *       Logback will not complain, it will simply not log the StructuredArgs.
     */
    private StructuredArgument[] extractParameters(String methodName, ProceedingJoinPoint joinPoint) {

        final String[] names = ((MethodSignature)joinPoint.getSignature()).getParameterNames();
        final Object[] values = joinPoint.getArgs();

        List<StructuredArgument> result = new LinkedList<StructuredArgument>();
        result.add(keyValue(LOG_CATEGORY_KEY, "internalCallDetails"));
        result.add(keyValue(LOGGED_API_NAME_KEY, methodName));

        for(int i=0; i<values.length; i++) {
            if(!EXCLUDED_PARAMS.contains(names[i])) {
                result.add(keyValue(names[i], values[i]));
            }
        }

        return result.toArray(new StructuredArgument[result.size()]);
    }

}
