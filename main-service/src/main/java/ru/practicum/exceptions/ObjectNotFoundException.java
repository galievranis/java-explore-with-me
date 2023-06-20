package ru.practicum.exceptions;

public class ObjectNotFoundException extends RuntimeException {

    public ObjectNotFoundException(String object, Long id) {
        super(String.format("%s with id=%d was not found.", object, id));
    }
}
