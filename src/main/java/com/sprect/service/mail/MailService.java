package com.sprect.service.mail;

import lombok.SneakyThrows;

public interface MailService {
    @SneakyThrows
    void sendActivationCode(String to, String subject);

    @SneakyThrows
    void sendSuspiciousActivity(String to, String subject);

    @SneakyThrows
    void sendEmailForChangePassword(String to, String subject);
}
