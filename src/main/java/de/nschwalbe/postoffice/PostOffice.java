package de.nschwalbe.postoffice;

import java.io.ByteArrayOutputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Service facade for sending mails.
 *
 * @author Nathanael Schwalbe
 * @since 05.04.2017
 */
public class PostOffice {

    private final MailStorage mailStorage;
    private final JavaMailSender mailSender;

    public PostOffice(MailStorage mailStorage, JavaMailSender mailSender) {
        this.mailStorage = mailStorage;
        this.mailSender = mailSender;
    }

    public PersistedMail postMail(MimeMessage mimeMessage) {

        byte[] content;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            mimeMessage.writeTo(out);
            content = out.toByteArray();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading mime message content!", e);
        }

        return mailStorage.create(content);
    }

    public MimeMessage createMimeMessage(String subject, String from, String[] to, String content, boolean isHtml) throws MessagingException {
        MimeMessageHelper messageHelper = createMimeMessageHelper(subject, from, to);
        messageHelper.setText(content, isHtml);
        return messageHelper.getMimeMessage();
    }

    public MimeMessage createMimeMessage(String subject, String from, String[] to, String html, String text) throws MessagingException {
        MimeMessageHelper messageHelper = createMimeMessageHelper(subject, from, to);
        messageHelper.setText(text, html);
        return messageHelper.getMimeMessage();
    }

    private MimeMessageHelper createMimeMessageHelper(String subject, String from, String[] to) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
        message.setSubject(subject);
        message.setFrom(from);
        message.setTo(to);
        return message;
    }
}
