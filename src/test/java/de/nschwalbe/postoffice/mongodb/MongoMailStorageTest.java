package de.nschwalbe.postoffice.mongodb;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import de.nschwalbe.postoffice.PostOfficeConfiguration;
import de.nschwalbe.postoffice.MailProcessState;

/**
 * Integration tests for the mongo mail storage.
 *
 * @author Nathanael Schwalbe
 * @since 05.04.2017
 */
@SpringBootTest({"spring.mail.host=localhost", "spring.mail.port=2525"})
public class MongoMailStorageTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MongoMailStorage mongoMailStorage;

    @Autowired
    private MongoOperations mongoOperations;

    private String mailId;

    @Test
    public void shouldInsertMail() {

        String message = "Email Content";
        MailDocument mailDocument = mongoMailStorage.create(message.getBytes(Charset.forName("UTF-8")));

        assertThat(mailDocument).isNotNull();
        assertThat(mailDocument.getId()).isNotNull();
        assertThat(mailDocument.getState()).isEqualTo(MailProcessState.NOT_SENT);

        this.mailId = mailDocument.getId();
        boolean exists = mongoOperations.exists(Query.query(Criteria.where("_id").is(mailId)), MailDocument.class);
        assertThat(exists).isTrue();
    }

    @Test(dependsOnMethods = "shouldInsertMail")
    public void shouldFindNotSentId() {

        List<String> ids = mongoMailStorage.findNotSentIds();

        assertThat(ids).hasSize(1);
        assertThat(ids).contains(mailId);
    }

    @Test(dependsOnMethods = "shouldFindNotSentId")
    public void shouldFindAndStartProgress() {

        MailDocument mailDocument = mongoMailStorage.findNotSentAndStartProgress(mailId);

        assertThat(mailDocument).isNotNull();
        assertThat(mailDocument.getState()).isEqualTo(MailProcessState.IN_PROGRESS);
    }

    @Test(dependsOnMethods = "shouldFindAndStartProgress")
    public void shouldDelete() {

        mongoMailStorage.delete(mailId);
        boolean exists = mongoOperations.exists(Query.query(Criteria.where("_id").is(mailId)), MailDocument.class);
        assertThat(exists).isFalse();
    }

    @SpringBootApplication
    @Import({ PostOfficeConfiguration.class})
    static class TestConfiguration {
    }
}
