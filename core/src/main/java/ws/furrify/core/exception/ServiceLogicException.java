package ws.furrify.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ServiceLogicException extends RuntimeException implements RestException {

    @Getter
    private final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public ServiceLogicException(String message) {
        super(message);
    }

}
