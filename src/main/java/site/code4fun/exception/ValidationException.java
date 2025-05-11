package site.code4fun.exception;

@SuppressWarnings("unused")
public class ValidationException extends RuntimeException{
    public ValidationException(String s) {
        super(s);
    }

    public ValidationException() {
        super();
    }

    public ValidationException(String s1, Object... s2) {
        super(String.format(s1, s2));
    }
}
