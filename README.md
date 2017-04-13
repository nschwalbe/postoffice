# Mail Module for asynchronous mail shipping

Persists the mail in a database and configures a scheduled task to actually send it.
This module is auto configured and depends on spring mail.

Currently only MongoDB is supported. For other databases implement the `MailStorage` interface.

## Installation

        <dependency>
            <groupId>de.upsource</groupId>
            <artifactId>spring-mailing</artifactId>
            <version>1.0.0</version>
        </dependency>

## Configuration
See spring mail configuration. 

<http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-email.html>

It uses the default ThreadPoolTaskScheduler which comes by default with only one thread. 
To configure the scheduler define a configuration class which implements `SchedulingConfigurer`.
For example:

    @Configuration
    @EnableScheduling
    @Profile("!test")
    public class TaskExecutionConfig implements SchedulingConfigurer {
    
        @Override
        public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
            ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
            threadPoolTaskScheduler.setPoolSize(3);
            threadPoolTaskScheduler.initialize();
            taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
        }
    }

## Usage

Define a Mailer class in your project and inject the `MailService`.

    @Component
    public class MyMailer {
    
        private final MailService mailService;
        private final TemplateEngine templateEngine;
        private final MessageSource messageSource;
    
        @Autowired
        public MyMailer(MailService mailService, TemplateEngine templateEngine, Environment environment, MessageSource messageSource) {
            this.mailService = mailService;
            this.templateEngine = templateEngine;
            this.messageSource = messageSource;
        }
    
        public void sendMail() {
    
            Context ctx = createContext();
    
            try {
                String subject = createSubject();
                String content = createContent(ctx);
    
                MimeMessage mimeMessage = mailService.createMimeMessage(subject, from, to, content, true);
    
                mailService.scheduleMail(mimeMessage);
    
            } catch (Exception e) {
                log.error("Could not create mail!", e);
            }
        }
    }
