package com.litethinking.platform.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.ses.model.SendRawEmailResponse;

import jakarta.activation.DataHandler;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

@Service
@Primary
@ConditionalOnProperty(prefix = "aws.ses", name = "enabled", havingValue = "true")
public class SesMailService implements MailService {

    private static final Logger log = LoggerFactory.getLogger(SesMailService.class);

    private final SesClient sesClient;
    private final String sender;

    public SesMailService(@Value("${aws.region}") String region,
                          @Value("${aws.ses.sender}") String sender) {
        this.sesClient = SesClient.builder()
                .region(Region.of(region))
                .build();
        this.sender = sender;
    }

    @Override
    public void sendInventoryEmail(String to, String subject, String body, byte[] attachment) {
        try {
            MimeMessage message = buildMimeMessage(to, subject, body, attachment);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);
            RawMessage rawMessage = RawMessage.builder()
                    .data(SdkBytes.fromByteArray(outputStream.toByteArray()))
                    .build();
            SendRawEmailRequest request = SendRawEmailRequest.builder()
                    .rawMessage(rawMessage)
                    .build();
            SendRawEmailResponse response = sesClient.sendRawEmail(request);
            log.info("Correo de inventario enviado via SES. MessageId={}", response.messageId());
        } catch (Exception e) {
            throw new IllegalStateException("Error enviando correo con SES", e);
        }
    }

    private MimeMessage buildMimeMessage(String to, String subject, String body, byte[] attachment) throws MessagingException {
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = new MimeMessage(session);
        message.setFrom(sender);
        message.addRecipients(jakarta.mail.Message.RecipientType.TO, to);
        message.setSubject(subject, "UTF-8");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body, "UTF-8");

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new DataHandler(new ByteArrayDataSource(attachment, "application/pdf")));
        attachmentPart.setFileName("inventario.pdf");

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);
        return message;
    }
}
