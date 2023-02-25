package com.justedlev.account.repository.entity;

import com.justedlev.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity
@Table(name = "phone_numbers")
public class PhoneNumber extends BaseEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "phone_number_id")
    private UUID id;
    @Column(name = "national")
    private Long national;
    @Column(name = "international")
    private String international;
    @Column(name = "country_code")
    private Integer countryCode;
    @Column(name = "region_code")
    private String regionCode;
    @ToString.Exclude
    @OneToOne(mappedBy = "phoneNumber")
    private Contact contact;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PhoneNumber that = (PhoneNumber) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
