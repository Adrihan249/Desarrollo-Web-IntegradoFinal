package com.taskmanager.exception;


/**
 * Excepción personalizada para recursos no encontrados
 * Se lanza cuando un usuario, proyecto, etc. no existe
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}