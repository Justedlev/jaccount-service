package com.justedlev.account.common.mapper.impl;

import com.justedlev.account.common.converter.PhoneNumberConverter;
import com.justedlev.account.common.mapper.AccountMapper;
import com.justedlev.account.common.mapper.BaseModelMapper;
import com.justedlev.account.model.Avatar;
import com.justedlev.account.model.request.AccountRequest;
import com.justedlev.account.model.response.AccountResponse;
import com.justedlev.account.model.response.ContactResponse;
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
import java.util.stream.Collectors;

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
        var res = mapper.map(request, AccountResponse.class);
        var contacts = convertToContactResponse(request);
        res.setContacts(contacts);

        return res;
    }

    private Set<ContactResponse> convertToContactResponse(Account request) {
        return request.getContacts()
                .stream()
                .map(current -> mapper.map(current, ContactResponse.class))
                .collect(Collectors.toSet());
    }

    @Override
    public Account map(AccountRequest request) {
        var account = mapper.map(request, Account.class);
        var contact = convertToContact(request);
        contact.setAccount(account);
        account.setContacts(Set.of(contact));

        return account;
    }

    @PostConstruct
    private void init() {
        mapper.createTypeMap(Account.class, AccountResponse.class)
                .addMapping(Account::getCreatedAt, AccountResponse::setRegistrationDate)
                .addMapping(this::getAvatarUrl, AccountResponse::setAvatarUrl);
    }

    private String getAvatarUrl(Account account) {
        return Optional.of(account)
                .map(Account::getAvatar)
                .map(Avatar::getUrl)
                .orElse(null);
    }

    private Contact convertToContact(AccountRequest accountRequest) {
        return Contact.builder()
                .phoneNumber(convertToPhone(accountRequest))
                .email(accountRequest.getEmail())
                .main(Boolean.TRUE)
                .build();
    }

    private PhoneNumber convertToPhone(AccountRequest accountRequest) {
        return Optional.ofNullable(accountRequest)
                .map(AccountRequest::getPhoneNumber)
                .filter(StringUtils::isNotBlank)
                .map(phoneNumberConverter::convert)
                .orElse(null);
    }
}
