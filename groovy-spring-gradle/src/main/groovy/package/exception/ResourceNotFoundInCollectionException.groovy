package ~~ROOT_PACKAGE~~.exception


class ResourceNotFoundInCollectionException extends RuntimeException {

    ResourceNotFoundInCollectionException(String collection, String id) {
        super(String.format("There is no item '%s' in the %s collection.", id, collection));
    }
}
