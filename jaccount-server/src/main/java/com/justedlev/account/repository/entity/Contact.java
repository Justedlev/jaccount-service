package com.justedlev.account.repository.entity;

import com.justedlev.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.Objects;
import java.util.UUID;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity
@Table(name = "contacts")
public class Contact extends BaseEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "contact_id")
    private UUID id;
    @Builder.Default
    @Column(name = "main", nullable = false)
    private boolean main = Boolean.FALSE;
    @Email
    @Column(name = "email", nullable = false)
    private String email;
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @Cascade({
            CascadeType.DETACH,
            CascadeType.MERGE,
            CascadeType.PERSIST,
            CascadeType.REFRESH,
            CascadeType.SAVE_UPDATE
    })
    @JoinTable(
            name = "contacts_phone_numbers",
            joinColumns = {@JoinColumn(name = "contact_id")},
            inverseJoinColumns = {@JoinColumn(name = "phone_number_id")}
    )
    private PhoneNumber phoneNumber;
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({
            CascadeType.DETACH,
            CascadeType.MERGE,
            CascadeType.PERSIST,
            CascadeType.REFRESH,
            CascadeType.SAVE_UPDATE
    })
    @JoinTable(
            name = "accounts_contacts",
            joinColumns = {@JoinColumn(name = "contact_id")},
            inverseJoinColumns = {@JoinColumn(name = "account_id")}
    )
    private Account account;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Contact contact = (Contact) o;
        return id != null && Objects.equals(id, contact.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
