package com.pickyboy.blingBackend.common.exception;

/**
 * JWT相关异常
 *
 * @author pickyboy
 */
public class JwtException extends RuntimeException {

    public JwtException(String message) {
        super(message);
    }

    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Token过期异常
     */
    public static class TokenExpiredException extends JwtException {
        public TokenExpiredException(String message) {
            super(message);
        }
    }

    /**
     * Token无效异常
     */
    public static class TokenInvalidException extends JwtException {
        public TokenInvalidException(String message) {
            super(message);
        }
    }

    /**
     * Token缺失异常
     */
    public static class TokenMissingException extends JwtException {
        public TokenMissingException(String message) {
            super(message);
        }
    }
}