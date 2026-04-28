package ws.furrify.core.exception.handler;

import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ws.furrify.core.exception.Errors;

@RestControllerAdvice
public class GlobalFeignExceptionHandler {

    @ExceptionHandler(NoFallbackAvailableException.class)
    public ProblemDetail handleNoFallbackAvailableException(NoFallbackAvailableException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                Errors.SERVICE_TEMPORARILY_UNAVAILABLE.getErrorMessage()
        );
    }

    @ExceptionHandler(feign.FeignException.ServiceUnavailable.class)
    public ProblemDetail handleServiceUnavailable(feign.FeignException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                Errors.SERVICE_TEMPORARILY_UNAVAILABLE.getErrorMessage()
        );
    }
}