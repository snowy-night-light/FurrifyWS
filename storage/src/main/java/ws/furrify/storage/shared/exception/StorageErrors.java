package ws.furrify.storage.shared.exception;

import lombok.RequiredArgsConstructor;
import ws.furrify.core.exception.ErrorMessageFormatterIntf;

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
public enum StorageErrors implements ErrorMessageFormatterIntf {
    /**
     * Exception types.
     */
    NO_TAG_FOUND("Tag [value={0}] was not found."),
    VIDEO_FRAME_EXTRACTION_FAILED("Video frame extraction for thumbnail has failed."),
    HARD_LIMIT_FOR_ENTITY_TYPE("Hard limit of [limit={0}] has been reached for [entity={1}], further create requests will not be accepted."),
    FILE_HASH_DUPLICATE_IN_POST("File with [md5={0}] hash already exists in this post with [uuid={1}].");



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