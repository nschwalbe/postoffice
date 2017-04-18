package de.nschwalbe.postoffice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.config.TriggerTask;

/**
 * Mail sending job and trigger.
 *
 * @author Nathanael Schwalbe
 * @since 05.04.2017
 */
class SendMailTaskFactory extends AbstractFactoryBean<TriggerTask> {

    private final MailStorage mailStorage;
    private final JavaMailSender javaMailSender;
    private final Environment env;

    SendMailTaskFactory(MailStorage mailStorage, JavaMailSender javaMailSender, Environment env) {
        this.mailStorage = mailStorage;
        this.javaMailSender = javaMailSender;
        this.env = env;
    }

    @Override
    public Class<?> getObjectType() {
        return TriggerTask.class;
    }

    @Override
    protected TriggerTask createInstance() throws Exception {
        SendMailTrigger trigger = new SendMailTrigger(env.getProperty("postoffice.worker.delay", Integer.class, 10));
        SendMailTask task = new SendMailTask(mailStorage, javaMailSender, trigger);
        return new TriggerTask(task, trigger);
    }

    static class SendMailTrigger implements Trigger {

        private int defaultDelay;
        private int delay;


        SendMailTrigger(int delay) {
            this.defaultDelay = delay;
            this.delay = delay;
        }

        // try again in 30s, then wait 60s, then wait 90s ...
        void increaseDelay() {
            delay += 30;

            // max delay between tries is 5 minutes
            if (delay > 300) {
                delay = 300;
            }
        }


        void resetDelay() {
            delay = defaultDelay;
        }

        @Override
        public Date nextExecutionTime(TriggerContext triggerContext) {

            Date lastRun = triggerContext.lastCompletionTime();
            ZonedDateTime nextRun;

            if (lastRun == null) {
                nextRun = ZonedDateTime.now().plusSeconds(delay);
            }
            else {
                nextRun = ZonedDateTime.ofInstant(lastRun.toInstant(), ZoneId.systemDefault()).plusSeconds(delay);
            }

            return Date.from(nextRun.toInstant());
        }
    }

    static class SendMailTask implements Runnable {

        private Logger log = LoggerFactory.getLogger(SendMailTask.class);

        private final MailStorage mailStorage;
        private final JavaMailSender javaMailSender;
        private final SendMailTrigger trigger;

        SendMailTask(MailStorage mailStorage, JavaMailSender javaMailSender,  SendMailTrigger trigger) {
            this.mailStorage = mailStorage;
            this.javaMailSender = javaMailSender;
            this.trigger = trigger;
        }

        public void run() {

            log.trace("Start mail shipping ...");

            List<String> mailIds = mailStorage.findNotSentIds();

            if (!mailIds.isEmpty()) {
                log.debug("Sending {} mails.", mailIds.size());
            }

            for (String mailId : mailIds) {

                PersistedMail mail = mailStorage.findNotSentAndStartProgress(mailId);

                if (mail == null) {
                    log.warn("Mail with id {} is not found any more. Skipping it.", mailId);
                    continue;
                }

                if (mail.getState() != MailProcessState.IN_PROGRESS) {
                    log.warn("Mail with id {} was not set to in_progress but is {}. Skipping it", mailId, mail.getState());
                    continue;
                }

                try (InputStream in = new ByteArrayInputStream(mail.getMimeMessageContent())) {

                    MimeMessage mimeMessage = javaMailSender.createMimeMessage(in);
                    javaMailSender.send(mimeMessage);
                    trigger.resetDelay();
                    updateMail(mail, MailProcessState.SENT, null);

                } catch (IOException e) {
                    log.error("Could not create MimeMessage from blob. Email could not be sent!", e);
                    updateMail(mail, MailProcessState.FAILED, e.getMessage());

                } catch (MailAuthenticationException e) {
                    log.error("Could not send mail because of incorrect credentials. __Fix email configuration!__ Trying to send this email again later.", e);
                    updateMail(mail, MailProcessState.NOT_SENT, e.getMessage());
                    trigger.increaseDelay();
                    break; // every other mail sending will also be failing

                } catch (MailSendException e) {
                    log.error("Could not send mail because of a network error! Trying again later.", e);
                    updateMail(mail, MailProcessState.NOT_SENT, e.getMessage());
                    trigger.increaseDelay();
                    break; // every other mail sending will also be failing

                } catch (MailException e) {
                    log.error("Email cannot be send and is thrown away!", e);
                    updateMail(mail, MailProcessState.FAILED, e.getMessage());
                }
            }

            log.trace("Finished mail shipping.");
        }

        private void updateMail(PersistedMail mail, MailProcessState state, String message) {
            mail.setState(state);
            mail.setErrorMessage(message);
            mailStorage.update(mail);
        }
    }
}
