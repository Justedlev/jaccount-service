package com.justedlev.account.component.impl;

import com.justedlev.account.component.MailComponent;
import com.justedlev.account.component.command.SendEmailCommand;
import com.justedlev.account.properties.ServiceProperties;
import com.justedlev.account.client.EndpointConstant;
import com.justedlev.account.constant.MailBodyConstant;
import com.justedlev.account.constant.MailSubjectConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
@RequiredArgsConstructor
public class MailComponentImpl implements MailComponent {
    private final ServiceProperties serviceProperties;
    private final JavaMailSender emailSender;

    @Override
    public void sendConfirmActivationMail(SendEmailCommand command) {
        var confirmUri = UriComponentsBuilder.fromHttpUrl(serviceProperties.getHost())
                .path(String.format("%s/%s", EndpointConstant.V1_ACCOUNT_CONFIRM, command.getActivationCode()))
                .build().toUriString();
        var confirmMailBody = String.format(MailBodyConstant.CONFIRM, command.getUserName(), confirmUri);

        sendMail(command.getRecipient(), confirmMailBody, MailSubjectConstant.CONFIRMATION);
    }

    private void sendMail(String to, String body, String subject) {
        log.info("Starting to send email : receiver={}", to);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(serviceProperties.getEmail());
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(String.format(subject, serviceProperties.getName()));
        simpleMailMessage.setText(body);
        emailSender.send(simpleMailMessage);
        log.info("Mail send successfully completed");
    }
}
