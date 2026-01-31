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
    CHAIN_OF_REQUESTS_BROKEN("There was an exception when contacting [name={0}] microservice."),
    CHAIN_OF_REQUESTS_UNAUTHORIZED("Chain of requests was broken with unauthorized call to [name={0}] microservice. " +
            "Make sure you have all roles required for this request."),
    FILE_UPLOAD_FAILED("File upload to remote storage has failed."),
    FILE_UPLOAD_CANNOT_CREATE_PATH("File upload can't create path to file."),
    FILE_CONTENT_IS_CORRUPTED("File content is corrupted."),
    FILE_EXTENSION_IS_NOT_MATCHING_CONTENT("File extension is not matching provided file content or filename is invalid."),
    MISSING_STRATEGY("Strategy was not found in given package [class={0}]."),
    FILENAME_IS_INVALID("Given filename [filename={0}] is invalid."),
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