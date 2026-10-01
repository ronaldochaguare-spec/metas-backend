package com.metas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String remitente;
    private final String destino;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String remitente,
                        @Value("${app.email-destino}") String destino) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.destino = destino;
    }

    public void enviar(String asunto, String cuerpo) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(remitente);
        msg.setTo(destino);
        msg.setSubject(asunto);
        msg.setText(cuerpo);
        mailSender.send(msg);
    }
}
