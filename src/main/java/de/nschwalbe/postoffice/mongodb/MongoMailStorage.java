package de.nschwalbe.postoffice.mongodb;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import de.nschwalbe.postoffice.MailProcessState;
import de.nschwalbe.postoffice.MailStorage;
import de.nschwalbe.postoffice.PersistedMail;

/**
 * Stores mails in mongodb.
 *
 * @author Nathanael Schwalbe
 * @since 05.04.2017
 */
class MongoMailStorage implements MailStorage {

    private final MongoOperations mongoOperations;

    MongoMailStorage(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public MailDocument create(byte[] mimeMessageContent) {
        MailDocument mailDocument = new MailDocument(mimeMessageContent);
        mongoOperations.save(mailDocument);
        return mailDocument;
    }

    @Override
    public List<String> findNotSentIds() {

        Query query = query(where("state").is(MailProcessState.NOT_SENT));
        query.fields().include("_id");

        List<MailDocument> mailDocuments = mongoOperations.find(query, MailDocument.class);

        return mailDocuments.stream().map(MailDocument::getId).collect(Collectors.toList());
    }

    @Override
    public MailDocument findNotSentAndStartProgress(String mailId) {

        Query query = query(where("_id").is(mailId).and("state").is(MailProcessState.NOT_SENT));
        Update update = Update.update("state", MailProcessState.IN_PROGRESS);
        return mongoOperations.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), MailDocument.class);
    }

    @Override
    public void delete(String id) {
        mongoOperations.remove(query(where("_id").is(id)), MailDocument.class);
    }

    @Override
    public void update(PersistedMail mail) {
        if (mail.getId() == null) {
            throw new IllegalArgumentException("Cannot update mail because it is not persisted yet.");
        }
        mongoOperations.save(mail);
    }
}
