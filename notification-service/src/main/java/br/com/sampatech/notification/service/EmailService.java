package br.com.sampatech.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.mail-from}")
    private String from;

    public void sendWelcome(String to, String name) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Bem-vindo(a) ao SampaTech!");
        msg.setText("""
                Olá, %s!

                Seu cadastro no SampaTech, o Portal da Transparência da zeladoria de São Paulo, foi concluído.

                Agora você pode consultar as ocorrências da sua região, acompanhar o histórico de cada uma
                e conversar com o nosso assistente.

                Equipe SampaTech
                """.formatted(name));
        mailSender.send(msg);
    }
}