package com.justedlev.account.repository;

import com.justedlev.account.repository.custom.ContactFilterableRepository;
import com.justedlev.account.repository.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID>, ContactFilterableRepository {
}
