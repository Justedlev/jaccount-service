package com.justedlev.account.component.impl;

import com.justedlev.account.component.ContactComponent;
import com.justedlev.account.repository.ContactRepository;
import com.justedlev.account.repository.entity.Contact;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContactComponentImpl implements ContactComponent {
    private final ContactRepository contactRepository;

    @Override
    public List<Contact> getByFilter() {
        return null;
    }
}
