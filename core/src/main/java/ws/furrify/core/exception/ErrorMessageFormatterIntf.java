package ws.furrify.core.exception;

public interface ErrorMessageFormatterIntf {
    String getErrorMessage(Object... data);

    String getErrorMessage(Object data);

    String getErrorMessage();
}
