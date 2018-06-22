package ~~ROOT_PACKAGE~~.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String collection, String id) {
        super(String.format("There is no item '%s' in the %s collection.", id, collection));
    }
}
