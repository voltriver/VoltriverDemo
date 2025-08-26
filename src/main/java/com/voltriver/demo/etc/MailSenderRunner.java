package com.voltriver.demo.etc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Component
@RequiredArgsConstructor
public class MailSenderRunner implements ApplicationRunner {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void run(ApplicationArguments args) {

    	try {
	        MimeMessage m = mailSender.createMimeMessage();
	        MimeMessageHelper h = new MimeMessageHelper(m,"UTF-8");
	        h.setFrom("neo073@naver.com");
	        h.setTo("voltriver7@gmail.com");
	        h.setSubject("테스트메일2");
	        h.setText("메일테스트2");
	        mailSender.send(m);
    	} catch(Exception e) {
    		log.error(String.format("mail send error, reason=%s", e.getMessage()));
    	}
    }
}