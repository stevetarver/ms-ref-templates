package ~~ROOT_PACKAGE~~.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for Contact data
 * I am left in place, to prove initial deploy
 * TODO: Delete me after initial deployment verifiction
 */
@Data
@NoArgsConstructor
public class Contact {
    private String id;
    private String firstName;
    private String lastName;
    private String companyName;
    private String address;
    private String city;
    private String county;
    private String state;
    private String zip;
    private String phone1;
    private String phone2;
    private String email;
    private String website;
}
