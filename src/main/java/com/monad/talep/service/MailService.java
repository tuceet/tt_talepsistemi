package com.monad.talep.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Gmail SMTP ile e-posta gonderir.
 * Uygulama sifresi ayarlanmadiysa hata firlatmaz; false doner,
 * ekranda gecici sifre gosterilerek akis yine calisir (demo dostu).
 */
@Service
public class MailService {

    private final ObjectProvider<JavaMailSender> senderProvider;

    @Value("${spring.mail.username:}")
    private String from;

    public MailService(ObjectProvider<JavaMailSender> senderProvider) {
        this.senderProvider = senderProvider;
    }

    public boolean send(String to, String subject, String body) {
        try {
            JavaMailSender sender = senderProvider.getIfAvailable();
            if (sender == null || from == null || from.isBlank()) return false;
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            sender.send(msg);
            return true;
        } catch (Exception e) {
            System.err.println("Mail gonderilemedi: " + e.getMessage());
            return false;
        }
    }

    public boolean sendPasswordReset(String to, String tempPassword) {
        return send(to, "TT Talep Destek - Sifre Sifirlama",
                "Merhaba,\n\nGecici sifreniz: " + tempPassword +
                "\n\nGiris yaptiktan sonra sifrenizi degistirmenizi oneririz.\n\nTT Talep Destek Sistemi");
    }

    public boolean sendWelcome(String to, String name) {
        return send(to, "TT Talep Destek - Hos Geldiniz",
                "Merhaba " + name + ",\n\nTT Talep Destek Sistemi'ne kaydiniz basariyla olusturuldu." +
                "\nArtik giris yapip talep olusturabilirsiniz.\n\nTT Talep Destek Sistemi");
    }
}
