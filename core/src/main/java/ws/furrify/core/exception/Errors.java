package ws.furrify.core.exception;

import lombok.RequiredArgsConstructor;

import java.text.MessageFormat;

/**
 * Class contains error messages enum which can be accessed using getErrorMessage().
 * Messages can contain parenthesis which can be filled using ex. Your id is {0}! and use method getErrorMessage(3);
 * Messages also can contain multiple parenthesis all can be filled using ex. Your id is {0} and your name is {1}! and use method getErrorMessage("3", "John")
 * It would be nice to also indicate what value was filled for ex. [uuid={0}].
 * <p>
 * Each exception that wants to use those error messages should be registered in RestExceptionControllerAdvice.
 *
 * @author Skyte
 */
@RequiredArgsConstructor
public enum Errors implements ErrorMessageFormatterIntf {
    /**
     * Exception types.
     */
    NO_RECORD_FOUND("Record [uuid={0}] was not found."),
    RECORD_ALREADY_EXISTS("Record [uuid={0}] already exists."),
    STRATEGY_NOT_FOUND("Strategy [strategy={0}] was not found."),
    DUPLICATE_STRATEGY_IN_APPLICATION("Strategy with name [strategy={0}] is duplicated within application."),
    BAD_REQUEST("Given request data is invalid."),
    SERVICE_TEMPORARILY_UNAVAILABLE("There was an issue contacting an external service. Please try again later."),
    MISSING_STRATEGY("Strategy was not found in given package [class={0}]."),
    USER_NOT_AUTHENTICATED("User was not properly authenticated for this action."),
    INTERNAL_SERVER_ERROR("Something went wrong. Please try again later."),
    UNIDENTIFIED("Unknown error occurred.");


    private final String errorMessage;

    public String getErrorMessage(Object... data) {
        return MessageFormat.format(errorMessage, data);
    }

    public String getErrorMessage(Object data) {
        return MessageFormat.format(errorMessage, data);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}