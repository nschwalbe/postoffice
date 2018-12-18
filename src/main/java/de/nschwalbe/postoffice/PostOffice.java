package de.nschwalbe.postoffice;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Service facade for sending mails.
 *
 * @author Nathanael Schwalbe
 * @since 05.04.2017
 */
public class PostOffice {

    private static final Logger log = LoggerFactory.getLogger(PostOffice.class);

    private final MailStorage mailStorage;
    private final JavaMailSender mailSender;

    public PostOffice(MailStorage mailStorage, JavaMailSender mailSender) {
        this.mailStorage = mailStorage;
        this.mailSender = mailSender;
    }

    /**
     * Creates a mail, stores it and sends it out later. This method returns immediately and does not wait for the mail server.
     *
     * @param subject the mail subject.
     * @param from the sender address.
     * @param to the recipient.
     * @param content the mail body.
     * @param isHtml true if mail body is html, if it is plain text set to false.
     * @return the persisted mail
     * @throws MessagingException if message creation failed due to some error.
     */
    public PersistedMail postMail(String subject, MailAddress from, MailAddress to, String content, boolean isHtml) throws MessagingException {
        MimeMessage mimeMessage = createMimeMessage(subject, from, Collections.singletonList(to), content, isHtml);
        return postMail(mimeMessage);
    }

    /**
     * Creates a mail, stores it and sends it out later. This method returns immediately and does not wait for the mail server.
     *
     * @param subject the mail subject.
     * @param from the sender address.
     * @param to the recipient.
     * @param html the mail body html part.
     * @param text the mail body text part.
     * @return the persisted mail.
     * @throws MessagingException if message creation failed due to some error.
     */
    public PersistedMail postMail(String subject, MailAddress from, MailAddress to, String html, String text) throws MessagingException {
        MimeMessage mimeMessage = createMimeMessage(subject, from, Collections.singletonList(to), html, text);
        return postMail(mimeMessage);
    }

    /**
     * Creates a mail, stores it and sends it out later. This method returns immediately and does not wait for the mail server.
     *
     * @param subject the mail subject.
     * @param from the sender address.
     * @param to the recipients.
     * @param content the mail body.
     * @param isHtml true if mail body is html, if it is plain text set to false.
     * @return the persisted mail
     * @throws MessagingException if message creation failed due to some error.
     */
    public PersistedMail postMail(String subject, MailAddress from, List<MailAddress> to, String content, boolean isHtml) throws MessagingException {
        MimeMessage mimeMessage = createMimeMessage(subject, from, to, content, isHtml);
        return postMail(mimeMessage);
    }

    /**
     * Creates a mail, stores it and sends it out later. This method returns immediately and does not wait for the mail server.
     *
     * @param subject the mail subject.
     * @param from the sender address.
     * @param to the recipients.
     * @param html the mail body html part.
     * @param text the mail body text part.
     * @return the persisted mail.
     * @throws MessagingException if message creation failed due to some error.
     */
    public PersistedMail postMail(String subject, MailAddress from, List<MailAddress> to, String html, String text) throws MessagingException {
        MimeMessage mimeMessage = createMimeMessage(subject, from, to, html, text);
        return postMail(mimeMessage);
    }

    private PersistedMail postMail(MimeMessage mimeMessage) throws MessagingException {

        byte[] content;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            mimeMessage.writeTo(out);
            content = out.toByteArray();
        } catch (Exception e) {
            throw new MessagingException("Error reading mime message content!", e);
        }

        return mailStorage.create(content);
    }

    private MimeMessage createMimeMessage(String subject, MailAddress from, List<MailAddress> to, String content, boolean isHtml) throws MessagingException {
        MimeMessageHelper messageHelper = createMimeMessageHelper(subject, from, to);
        messageHelper.setText(content, isHtml);
        return messageHelper.getMimeMessage();
    }

    private MimeMessage createMimeMessage(String subject, MailAddress from, List<MailAddress> to, String html, String text) throws MessagingException {
        MimeMessageHelper messageHelper = createMimeMessageHelper(subject, from, to);
        messageHelper.setText(text, html);
        return messageHelper.getMimeMessage();
    }

    private MimeMessageHelper createMimeMessageHelper(String subject, MailAddress from, List<MailAddress> to) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
        message.setSubject(subject);
        try {
            message.setFrom(from.getAddress(), from.getPersonal());
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Error creating From-Address", e);
        }

        InternetAddress[] internetAddresses = to.stream()
            .map(mailAddress -> {
                try {
                    return new InternetAddress(mailAddress.getAddress(), mailAddress.getPersonal(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    log.error("This should never happen!", e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toArray(InternetAddress[]::new);
        message.setTo(internetAddresses);
        return message;
    }
}
