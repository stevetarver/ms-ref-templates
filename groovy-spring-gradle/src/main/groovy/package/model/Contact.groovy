package ~~ROOT_PACKAGE~~.model

import org.hibernate.validator.constraints.NotEmpty


class Contact {
    String id;
    @NotEmpty
    String firstName;
    @NotEmpty
    String lastName;
    String companyName;
    String address;
    String city;
    String county;
    String state;
    @NotEmpty
    String zip;
    String phone1;
    String phone2;
    @NotEmpty
    String email;
    String website;
}
