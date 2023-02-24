package com.justedlev.account.service.impl;

import com.justedlev.account.client.EndpointConstant;
import com.justedlev.account.common.mapper.AccountMapper;
import com.justedlev.account.common.mapper.ReportMapper;
import com.justedlev.account.component.AccountComponent;
import com.justedlev.account.component.AccountModeComponent;
import com.justedlev.account.constant.ExceptionConstant;
import com.justedlev.account.constant.MailSubjectConstant;
import com.justedlev.account.model.params.AccountFilterParams;
import com.justedlev.account.model.request.AccountRequest;
import com.justedlev.account.model.request.UpdateAccountModeRequest;
import com.justedlev.account.model.response.AccountResponse;
import com.justedlev.account.properties.JAccountProperties;
import com.justedlev.account.repository.custom.filter.AccountFilter;
import com.justedlev.account.repository.entity.Account;
import com.justedlev.account.repository.entity.Contact;
import com.justedlev.account.service.AccountService;
import com.justedlev.common.model.request.PaginationRequest;
import com.justedlev.common.model.response.PageResponse;
import com.justedlev.common.model.response.ReportResponse;
import com.justedlev.notification.model.request.SendTemplateMailRequest;
import com.justedlev.notification.queue.JNotificationQueue;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountComponent accountComponent;
    private final AccountMapper accountMapper;
    private final ReportMapper reportMapper;
    private final AccountModeComponent accountModeComponent;
    private final JNotificationQueue notificationQueue;
    private final JAccountProperties properties;
    private final ModelMapper modelMapper;

    @Override
    public PageResponse<AccountResponse> getPage(PaginationRequest request) {
        var page = accountComponent.getPage(request.toPegeable())
                .map(accountMapper::map);

        return PageResponse.from(page);
    }

    @Override
    public PageResponse<AccountResponse> getPageByFilter(AccountFilterParams params, PaginationRequest pagination) {
        var filter = modelMapper.typeMap(AccountFilterParams.class, AccountFilter.class)
                .addMapping(AccountFilterParams::getQ, AccountFilter::setSearchText)
                .map(params);
        var page = accountComponent.getPageByFilter(filter, pagination.toPegeable())
                .map(accountMapper::map);

        return PageResponse.from(page);
    }

    @Override
    public AccountResponse getByEmail(String email) {
        var filter = AccountFilter.builder()
                .emails(Set.of(email))
                .build();
        var account = accountComponent.getByFilter(filter)
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(ExceptionConstant.USER_NOT_EXISTS, email)));

        return accountMapper.map(account);
    }

    @Override
    public AccountResponse getByNickname(String nickname) {
        var account = accountComponent.getByNickname(nickname)
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(ExceptionConstant.USER_NOT_EXISTS, nickname)));

        return accountMapper.map(account);
    }

    @Override
    public ReportResponse confirm(String code) {
        var account = accountComponent.confirm(code);

        return reportMapper.toReport(String.format("User %s confirmed account", account.getNickname()));
    }

    @Override
    public AccountResponse update(String nickname, AccountRequest request) {
        var account = accountComponent.update(nickname, request);

        return accountMapper.map(account);
    }

    @Override
    public AccountResponse updateAvatar(String nickname, MultipartFile photo) {
        var account = accountComponent.update(nickname, photo);

        return accountMapper.map(account);
    }

    @Override
    public List<AccountResponse> updateMode(UpdateAccountModeRequest request) {
        return accountModeComponent.updateMode(request);
    }

    @Override
    public AccountResponse create(AccountRequest request) {
        var account = accountComponent.create(request);
        var saved = accountComponent.save(account);
        sendConfirmationEmail(saved);

        return accountMapper.map(saved);
    }

    @SneakyThrows
    private void sendConfirmationEmail(Account account) {
        var confirmationLink = UriComponentsBuilder.fromHttpUrl(properties.getService().getHost())
                .path(EndpointConstant.V1_ACCOUNT_CONFIRM)
                .path("/" + account.getActivationCode())
                .build().toUriString();
        var content = Map.of(
                "{FULL_NAME}", account.getNickname(),
                "{CONFIRMATION_LINK}", confirmationLink,
                "{BEST_REGARDS_FROM}", properties.getService().getName()
        );
        var recipient = account.findPrimaryContact()
                .map(Contact::getEmail)
                .orElseThrow(() -> new EntityNotFoundException("Cant find email for account " + account.getId()));
        var mail = SendTemplateMailRequest.builder()
                .recipient(recipient)
                .subject(String.format(MailSubjectConstant.CONFIRMATION, properties.getService().getName()))
                .templateName("account-confirmation")
                .content(content)
                .build();
        notificationQueue.sendEmail(mail);
    }
}
