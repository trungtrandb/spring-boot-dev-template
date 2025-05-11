package site.code4fun.exception;

// 27. SessionExpiredException
public class SessionExpiredException extends RuntimeException {
    public SessionExpiredException(String message) {
        super(message);
    }
}
