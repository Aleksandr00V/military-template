package ua.edu.viti.military.exception;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s не знайдено з %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
