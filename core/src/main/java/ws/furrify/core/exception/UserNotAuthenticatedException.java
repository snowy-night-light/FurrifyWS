package ws.furrify.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class UserNotAuthenticatedException extends RuntimeException implements RestException {

    @Getter
    private final HttpStatus status = HttpStatus.UNAUTHORIZED;

    public UserNotAuthenticatedException(String message) {
        super(message);
    }

}
