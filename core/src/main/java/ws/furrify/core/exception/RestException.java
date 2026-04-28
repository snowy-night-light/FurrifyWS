package ws.furrify.core.exception;

/**
 * Custom REST exception template interface.
 */
public interface RestException {

    /**
     * Get exception message. It can be one of the pre-created ones in Errors enum or your own.
     *
     * @return String value of message.
     */
    String getMessage();
}