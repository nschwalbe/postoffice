package de.nschwalbe.postoffice.mongodb;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;

import de.nschwalbe.postoffice.MailStorage;

/**
 * Configuration for the mongo storage.
 *
 * @author Nathanael Schwalbe
 * @since 06.04.2017
 */
@ConditionalOnMissingBean(MailStorage.class)
@ConditionalOnClass(MongoOperations.class)
@Configuration
public class MongoMailStorageConfiguration {

    @Bean
    public MongoMailStorage mongoMailStorage(MongoOperations mongoOperations) {
        return new MongoMailStorage(mongoOperations);
    }
}
