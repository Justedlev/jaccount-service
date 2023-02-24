package com.justedlev.account.common.mapper.impl;

import com.justedlev.account.common.converter.PhoneNumberConverter;
import com.justedlev.account.common.mapper.AccountMapper;
import com.justedlev.account.common.mapper.BaseModelMapper;
import com.justedlev.account.model.Avatar;
import com.justedlev.account.model.request.AccountRequest;
import com.justedlev.account.model.response.AccountResponse;
import com.justedlev.account.repository.entity.Account;
import com.justedlev.account.repository.entity.Contact;
import com.justedlev.account.repository.entity.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AccountMapperImpl implements AccountMapper {
    private final ModelMapper mapper = new BaseModelMapper();
    private final PhoneNumberConverter phoneNumberConverter;

    @Override
    public ModelMapper getMapper() {
        return this.mapper;
    }

    @Override
    public AccountResponse map(Account request) {
        return mapper.map(request, AccountResponse.class);
    }

    @Override
    public Account map(AccountRequest request) {
        return mapper.map(request, Account.class);
    }

    @PostConstruct
    private void init() {
        mapper.createTypeMap(Account.class, AccountResponse.class)
                .addMapping(Account::getCreatedAt, AccountResponse::setRegistrationDate)
                .addMapping(this::getAvatarUrl, AccountResponse::setAvatarUrl);
//                .addMapping(account -> account.getContacts()
//                        .stream()
//                        .map(Contact::getPhoneNumber)
//                        .map(current -> PhoneNumberResponse.builder()
//                                .countryCode(current.getCountryCode())
//                                .national(current.getNational())
//                                .international(current.getInternational())
//                                .regionCode(current.getRegionCode())
//                                .build())
//                        .collect(Collectors.toSet()), AccountResponse::setPhoneNumbers)
//                .addMapping(account -> account.getContacts()
//                        .stream()
//                        .map(Contact::getEmail)
//                        .collect(Collectors.toSet()), AccountResponse::setEmails);
        mapper.createTypeMap(AccountRequest.class, Account.class)
                .addMapping(this::convertToContacts, Account::setContacts);
    }

    private Set<Contact> convertToContacts(AccountRequest accountRequest) {
        var contact = Contact.builder()
                .phoneNumber(convertToPhone(accountRequest))
                .email(accountRequest.getEmail())
                .build();

        return Set.of(contact);
    }

    private String getAvatarUrl(Account account) {
        return Optional.of(account)
                .map(Account::getAvatar)
                .map(Avatar::getUrl)
                .orElse(null);
    }

    private PhoneNumber convertToPhone(AccountRequest accountRequest) {
        return Optional.ofNullable(accountRequest)
                .map(AccountRequest::getPhoneNumber)
                .filter(StringUtils::isNotBlank)
                .map(phoneNumberConverter::convert)
                .orElse(null);
    }
}
