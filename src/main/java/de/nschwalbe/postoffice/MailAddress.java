package de.nschwalbe.postoffice;

import java.util.Objects;

/**
 * A mail address.
 *
 * @author Nathanael Schwalbe
 * @since 14.04.2017
 */
public class MailAddress {

    private final String address;
    private final String personal;

    private MailAddress(String address, String personal) {
        this.address = Objects.requireNonNull(address);
        this.personal = personal;
    }

    public static MailAddress of(String address, String personal) {
        return new MailAddress(address, personal);
    }

    public static MailAddress of(String address) {
        return new MailAddress(address, null);
    }

    public String getAddress() {
        return address;
    }

    public String getPersonal() {
        return personal;
    }
}
