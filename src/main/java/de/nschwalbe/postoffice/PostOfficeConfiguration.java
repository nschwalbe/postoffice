package de.nschwalbe.postoffice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
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

    @Autowired
    private Environment env;

    @Bean
    public PostOffice postOffice(MailStorage mailStorage) {
        return new PostOffice(mailStorage, javaMailSender);
    }

    @Bean("sendMailTask")
    public SendMailTaskFactory sendMailTaskFactory(MailStorage mailStorage) {
        return new SendMailTaskFactory(mailStorage, javaMailSender, env);
    }

    @EnableScheduling
    @Configuration
    static class SchedulerConfiguration implements SchedulingConfigurer {

        @Autowired
        private TriggerTask sendMailTask;

        @Override
        public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
            try {
                taskRegistrar.addTriggerTask(sendMailTask);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
