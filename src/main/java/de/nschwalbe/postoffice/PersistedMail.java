package de.nschwalbe.postoffice;

import java.time.LocalDateTime;

/**
 * Defines a mail to be stored in a database.
 *
 * @author Nathanael Schwalbe
 * @since 05.04.2017
 */
public interface PersistedMail {

    String getId();

    LocalDateTime getCreatedDate();
    LocalDateTime getLastModifiedDate();

    byte[] getMimeMessageContent();

    MailProcessState getState();
    void setState(MailProcessState state);

    String getErrorMessage();
    void setErrorMessage(String message);
}
