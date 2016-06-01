package org.paumard.spliterators.exception;

/**
 * Created by José on 01/06/2016.
 */
public class WhyWouldYouDoThatException extends RuntimeException {

    public WhyWouldYouDoThatException() {}

    public WhyWouldYouDoThatException(String message) {
        super(message);
    }

    public WhyWouldYouDoThatException(String message, Throwable cause) {
        super(message, cause);
    }

    public WhyWouldYouDoThatException(Throwable cause) {
        super(cause);
    }
}
