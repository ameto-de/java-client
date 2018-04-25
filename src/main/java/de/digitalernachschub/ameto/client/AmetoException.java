package de.digitalernachschub.ameto.client;

import lombok.RequiredArgsConstructor;

/**
 * Base class for all exceptions thrown by the Ameto client.
 */
public class AmetoException extends RuntimeException {
    public AmetoException(String message) {
        super(message);
    }

    public AmetoException(String message, Throwable cause) {
        super(message, cause);
    }
}
