package ws.furrify.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReferenceNotFoundException extends RuntimeException implements RestException {
    public ReferenceNotFoundException(String message) {
        super(message);
    }

}
