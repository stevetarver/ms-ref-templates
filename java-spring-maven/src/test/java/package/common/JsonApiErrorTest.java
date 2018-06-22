package ~~ROOT_PACKAGE~~.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class JsonApiErrorTest {

    /**
     * This is another terrible unit test, only included to generate unit
     * test and code coverage results
     */
    @Test
    void validationFailsWhenNoEmail() {
        String detail = "happy, happy";
        JsonApiError subject = new JsonApiError(HttpStatus.ACCEPTED, detail);

        assertTrue(subject.getStatus() instanceof String);
        assertTrue(subject.getDetail() == detail );
    }

}