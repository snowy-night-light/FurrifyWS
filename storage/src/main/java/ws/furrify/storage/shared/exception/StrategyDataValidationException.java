package ws.furrify.storage.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ws.furrify.core.exception.RestException;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StrategyDataValidationException extends RuntimeException implements RestException {
    public StrategyDataValidationException(String message) {
        super(message);
    }

}
