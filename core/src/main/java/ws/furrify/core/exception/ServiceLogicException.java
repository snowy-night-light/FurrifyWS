package ws.furrify.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ServiceLogicException extends RuntimeException implements RestException {
    public ServiceLogicException(String message) {
        super(message);
    }

}
