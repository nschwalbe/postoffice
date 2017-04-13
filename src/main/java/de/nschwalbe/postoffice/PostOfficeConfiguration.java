package de.nschwalbe.postoffice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;

/**
 * Configuration of the mailing module.
 *
 * @author Nathanael Schwalbe
 * @since 05.04.2017
 */
@Configuration
public class PostOfficeConfiguration {

    @Autowired
    private JavaMailSender javaMailSender;

    @Bean
    public PostOffice mailService(MailStorage mailStorage) {
        return new PostOffice(mailStorage, javaMailSender);
    }

    @Bean("mailSendingTriggerTask")
    public MailSendingTriggerTaskFactory mailSendingTriggerTaskFactory(MailStorage mailStorage) {
        return new MailSendingTriggerTaskFactory(mailStorage, javaMailSender);
    }

    @EnableScheduling
    @Configuration
    static class SchedulerConfiguration implements SchedulingConfigurer {

        @Autowired
        private TriggerTask mailSendingTriggerTask;

        // TODO not for test
        @Override
        public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
            try {
                taskRegistrar.addTriggerTask(mailSendingTriggerTask);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
