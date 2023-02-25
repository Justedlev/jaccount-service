package com.justedlev.account.component.impl;

import com.justedlev.account.client.EndpointConstant;
import com.justedlev.account.component.NotificationComponent;
import com.justedlev.account.constant.MailSubjectConstant;
import com.justedlev.account.properties.JAccountProperties;
import com.justedlev.account.repository.entity.Account;
import com.justedlev.account.repository.entity.Contact;
import com.justedlev.notification.model.request.SendTemplateMailRequest;
import com.justedlev.notification.queue.JNotificationQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationComponentImpl implements NotificationComponent {
    private final JNotificationQueue notificationQueue;
    private final JAccountProperties properties;

    @Override
    public void sendConfirmationEmail(Account account) {
        var confirmationLink = UriComponentsBuilder.fromHttpUrl(properties.getService().getHost())
                .path(EndpointConstant.V1_ACCOUNT_CONFIRM)
                .path("/" + account.getActivationCode())
                .build().toUriString();
        var content = Map.of(
                "{FULL_NAME}", account.getNickname(),
                "{CONFIRMATION_LINK}", confirmationLink,
                "{BEST_REGARDS_FROM}", properties.getService().getName()
        );
        var recipient = account.getContacts()
                .stream()
                .min(Comparator.comparing(Contact::getCreatedAt))
                .map(Contact::getEmail)
                .orElseThrow(() -> new EntityNotFoundException("Cant find contact for account " + account.getId()));
        var mail = SendTemplateMailRequest.builder()
                .recipient(recipient)
                .subject(String.format(MailSubjectConstant.CONFIRMATION, properties.getService().getName()))
                .templateName("account-confirmation")
                .content(content)
                .build();
        notificationQueue.sendEmail(mail);
    }
}
