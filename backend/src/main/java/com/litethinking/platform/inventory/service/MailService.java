package com.litethinking.platform.inventory.service;

public interface MailService {
    void sendInventoryEmail(String to, String subject, String body, byte[] attachment);
}
