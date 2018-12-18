package de.nschwalbe.postoffice;

import java.util.List;

/**
 * Persists a mail to be send by a scheduled task.
 *
 * @author Nathanael Schwalbe
 * @since 05.04.2017
 */
public interface MailStorage {

    PersistedMail create(byte[] mimeMessageContent);

    List<String> findNotSentIds();

    PersistedMail findNotSentAndStartProgress(String mailId);

    void delete(String id);

    void update(PersistedMail mail);
}
