package com.litethinking.platform.inventory.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

@Service
@ConditionalOnProperty(prefix = "mail", name = "provider", havingValue = "sendgrid")
public class SendGridMailService implements MailService {

    private static final Logger log = LoggerFactory.getLogger(SendGridMailService.class);

    private final SendGrid sendGrid;
    private final String sender;

    public SendGridMailService(
            @Value("${mail.sendgrid.api-key}") String apiKey,
            @Value("${mail.sender}") String sender
    ) {
        this.sendGrid = new SendGrid(apiKey);
        this.sender = sender;
    }

    @Override
    public void sendInventoryEmail(String to, String subject, String body, byte[] attachment) {
        Email from = new Email(sender);
        Email recipient = new Email(to);
        Content content = new Content("text/plain", body);

        Mail mail = new Mail(from, subject, recipient, content);
        Attachments pdf = new Attachments();
        pdf.setFilename("inventario.pdf");
        pdf.setType("application/pdf");
        pdf.setDisposition("attachment");
        pdf.setContent(Base64.getEncoder().encodeToString(attachment));
        mail.addAttachments(pdf);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Correo enviado correctamente via SendGrid. Status: {}", response.getStatusCode());
            } else {
                log.warn("Fallo al enviar correo via SendGrid. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                throw new IllegalStateException("Fallo al enviar correo via SendGrid");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error al enviar correo via SendGrid", e);
        }
    }
}
