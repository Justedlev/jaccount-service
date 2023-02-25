package com.justedlev.account.component;

import com.justedlev.account.repository.entity.Account;

public interface NotificationComponent {
    void sendConfirmationEmail(Account account);
}
