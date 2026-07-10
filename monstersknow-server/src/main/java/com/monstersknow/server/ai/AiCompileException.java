package com.monstersknow.server.ai;

/**
 * Thrown when a user-submitted AI plugin fails to compile, load, or
 * instantiate. The message carries a human-readable explanation (compiler
 * diagnostics or a reflection failure) suitable for returning to the caller.
 */
public class AiCompileException extends Exception {
    public AiCompileException(String message) {
        super(message);
    }

    public AiCompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
