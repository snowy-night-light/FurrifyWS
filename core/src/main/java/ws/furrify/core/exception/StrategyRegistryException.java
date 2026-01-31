package ws.furrify.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class StrategyRegistryException extends RuntimeException implements RestException {

    @Getter
    private final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public StrategyRegistryException(String message) {
        super(message);
    }

}
