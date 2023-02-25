package com.justedlev.account.component;

import com.justedlev.account.repository.entity.Contact;

import java.util.List;

public interface ContactComponent {
    List<Contact> getByFilter();
}
