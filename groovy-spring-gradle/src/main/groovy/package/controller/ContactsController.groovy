package ~~ROOT_PACKAGE~~.controller

import ~~ROOT_PACKAGE~~.common.aspect.LoggedApi
import ~~ROOT_PACKAGE~~.model.Contact
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@LoggedApi
@RestController
@RequestMapping(
        value = "~~ROOT_URI~~/contacts",
        produces = "application/json")
class ContactsController {

    /**
     * Find contacts
     *
     * json:api says: the result of an empty search is OK, not NOT_FOUND
     *
     * TODO: I am left in place for intial deployment validation - remove me
     *       when that task is complete
     */
    @GetMapping
    ResponseEntity findContacts() {
        return new ResponseEntity<>([new Contact(id: 'bea8f', firstName: 'Lester')], HttpStatus.OK)
    }

}
