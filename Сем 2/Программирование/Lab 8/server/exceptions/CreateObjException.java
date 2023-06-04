package exceptions;

/**
 * An exception that is thrown when an object cannot be created.
 */
public class CreateObjException extends Exception {

    public CreateObjException(String msg) {
        super(msg);
    }
}