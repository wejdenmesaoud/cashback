package com.bezkoder.springjwt.exception;

/**
 * Utility class for throwing exceptions in a consistent way
 */
public class ExceptionUtils {
    
    /**
     * Throws a ResourceNotFoundException if the condition is true
     * 
     * @param condition the condition to check
     * @param resourceName the name of the resource
     * @param fieldName the name of the field
     * @param fieldValue the value of the field
     */
    public static void throwIfNotFound(boolean condition, String resourceName, String fieldName, Object fieldValue) {
        if (condition) {
            throw new ResourceNotFoundException(resourceName, fieldName, fieldValue);
        }
    }
    
    /**
     * Throws a DuplicateResourceException if the condition is true
     * 
     * @param condition the condition to check
     * @param resourceName the name of the resource
     * @param fieldName the name of the field
     * @param fieldValue the value of the field
     */
    public static void throwIfDuplicate(boolean condition, String resourceName, String fieldName, Object fieldValue) {
        if (condition) {
            throw new DuplicateResourceException(resourceName, fieldName, fieldValue);
        }
    }
    
    /**
     * Throws an InvalidDataException if the condition is true
     * 
     * @param condition the condition to check
     * @param message the error message
     */
    public static void throwIfInvalid(boolean condition, String message) {
        if (condition) {
            throw new InvalidDataException(message);
        }
    }
    
    /**
     * Throws an OperationNotAllowedException if the condition is true
     * 
     * @param condition the condition to check
     * @param message the error message
     */
    public static void throwIfNotAllowed(boolean condition, String message) {
        if (condition) {
            throw new OperationNotAllowedException(message);
        }
    }
}
