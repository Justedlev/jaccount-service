package com.justedlev.account.repository.entity;

import com.justedlev.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Email;
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
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "phone_number_id", referencedColumnName = "phone_number_id")
    private PhoneNumber phoneNumber;
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
