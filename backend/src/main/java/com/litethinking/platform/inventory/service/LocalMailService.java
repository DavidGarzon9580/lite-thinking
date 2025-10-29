package com.litethinking.platform.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
public class LocalMailService implements MailService {

    private static final Logger log = LoggerFactory.getLogger(LocalMailService.class);

    @Override
    public void sendInventoryEmail(String to, String subject, String body, byte[] attachment) {
        log.info("Simulando envio de correo a {} con asunto '{}'. PDF adjunto de {} bytes.", to, subject, attachment.length);
        // En un entorno con AWS disponible, reemplazar por implementacion SES real.
    }
}
