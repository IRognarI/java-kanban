package exception;

public class TaskTimeValidationException extends RuntimeException {
    public TaskTimeValidationException(String message) {
        super(message);
    }
}
