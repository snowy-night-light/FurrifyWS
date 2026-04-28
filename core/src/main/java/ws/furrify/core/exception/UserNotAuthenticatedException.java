package ws.furrify.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UserNotAuthenticatedException extends RuntimeException implements RestException {

    public UserNotAuthenticatedException(String message) {
        super(message);
    }

}
