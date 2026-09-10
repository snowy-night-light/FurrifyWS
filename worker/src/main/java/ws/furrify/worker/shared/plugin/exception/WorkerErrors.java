package ws.furrify.worker.shared.plugin.exception;

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
public enum WorkerErrors implements ErrorMessageFormatterIntf {
    /**
     * Exception types.
     **/
     TASK_DOESNT_ALLOW_EXECUTION_WITH_STATUS("Task [id={0}] doesn't not allow execution with task status [status={1}]."),
     TASK_DOESNT_ALLOW_UPDATE_WITH_STATUS("Task [id={0}] doesn't not allow update with task status [status={1}]."),
    TASK_DOESNT_ALLOW_REMOVAL_WITH_STATUS("Task [id={0}] can't be removed with task status [status={1}].");

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