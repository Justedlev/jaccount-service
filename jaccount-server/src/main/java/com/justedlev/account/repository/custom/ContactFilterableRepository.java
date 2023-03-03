package com.justedlev.account.repository.custom;

import com.justedlev.account.repository.custom.filter.ContactFilter;
import com.justedlev.account.repository.entity.Contact;

public interface ContactFilterableRepository extends FilterableRepository<Contact, ContactFilter> {
}
