package no.java.moosehead;

public class MoosheadException extends RuntimeException {
    public MoosheadException() {
    }

    public MoosheadException(String message) {
        super(message);
    }

    public MoosheadException(String message, Throwable cause) {
        super(message, cause);
    }

    public MoosheadException(Throwable cause) {
        super(cause);
    }

    public MoosheadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
