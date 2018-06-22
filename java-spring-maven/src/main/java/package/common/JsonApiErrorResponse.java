package ~~ROOT_PACKAGE~~.common;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * I wrap JsonApiErrors in a response top-level "errors" object.
 */
@Data
@NoArgsConstructor
public class JsonApiErrorResponse {

    public JsonApiErrorResponse(JsonApiError jsonApiError) {
        this.errors = new JsonApiError[1];
        this.errors[0] = jsonApiError;
    }

    private JsonApiError[] errors;
}
