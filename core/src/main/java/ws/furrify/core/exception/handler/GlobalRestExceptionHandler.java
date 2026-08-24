package ws.furrify.core.exception.handler;


import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ws.furrify.core.specification.EntitySpec;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Object> handlePropertyReferenceException(PropertyReferenceException ex) {
        return createBadRequestResponse(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        return createBadRequestResponse(ex.getMessage());
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<Object> handleInvalidDataAccessResourceUsageException(InvalidDataAccessResourceUsageException ex) {
        if (!isFromEntitySpec(ex)) {
            throw ex;
        }
        
        String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        return createBadRequestResponse(message);
    }

    private boolean isFromEntitySpec(Throwable ex) {
        while (ex != null) {
            for (StackTraceElement element : ex.getStackTrace()) {
                if (element.getClassName().startsWith(EntitySpec.class.getName())) {
                    return true;
                }
            }
            ex = ex.getCause();
        }
        return false;
    }

    private ResponseEntity<Object> createBadRequestResponse(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", ZonedDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", message);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
