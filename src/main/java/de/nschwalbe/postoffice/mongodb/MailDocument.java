package de.nschwalbe.postoffice.mongodb;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import de.nschwalbe.postoffice.MailProcessState;
import de.nschwalbe.postoffice.PersistedMail;

/**
 * A mail persisted with mongodb. Mails will be automatically removed after 3 days if send or not.
 *
 * @author Nathanael Schwalbe
 * @since 05.04.2017
 */
@Document(collection = "mails")
class MailDocument implements PersistedMail {

    @Id
    private String id;

    @Indexed(expireAfterSeconds = 259200)
    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    private MailProcessState state = MailProcessState.NOT_SENT;
    private String errorMessage;

    private final byte[] mimeMessageContent;

    @PersistenceConstructor
    MailDocument(byte[] mimeMessageContent) {
        this.mimeMessageContent = mimeMessageContent;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public MailProcessState getState() {
        return state;
    }

    @Override
    public void setState(MailProcessState state) {
        this.state = state;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public byte[] getMimeMessageContent() {
        return mimeMessageContent;
    }

    @Override
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
}
