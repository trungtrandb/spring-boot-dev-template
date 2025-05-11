package site.code4fun.exception;

@SuppressWarnings("unused")
public class ServiceException extends RuntimeException{
    public ServiceException(String s) {
        super(s);
    }

    public ServiceException() {
        super();
    }
}
