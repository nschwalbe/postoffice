# Post Office

Bring your mail to the post office. It will be stored and a postal worker will deliver it.
Because of the always recurring task in every project to store a mail and send it out with a worker I extracted this as a 
module.

This module is auto configured and depends on spring mail.

Currently only MongoDB is supported. For other databases implement the `MailStorage` interface.

## Installation

TODO maven dependency from github https://jitpack.io/


## Configuration
### Mail Server
Configure at least the `spring.mail.host` property. For more information see the spring mail configuration. 

<http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-email.html>

### Thread Pool
The default `ThreadPoolTaskScheduler` is used which comes by default with only one thread. 
To configure the scheduler define a configuration class which implements `SchedulingConfigurer`.
For example:

```java
@Configuration
@EnableScheduling
public class TaskExecutionConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(3);
        threadPoolTaskScheduler.initialize();
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }
}
```

## Usage

Define a Mailer class in your project and inject the `MailService`.

```java
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

            mailService.postMail(mimeMessage);

        } catch (Exception e) {
            log.error("Could not create mail!", e);
        }
    }
}
```
