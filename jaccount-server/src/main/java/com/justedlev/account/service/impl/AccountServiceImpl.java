package com.justedlev.account.service.impl;

import com.justedlev.account.common.mapper.AccountMapper;
import com.justedlev.account.common.mapper.ReportMapper;
import com.justedlev.account.component.AccountComponent;
import com.justedlev.account.component.AccountModeComponent;
import com.justedlev.account.component.NotificationComponent;
import com.justedlev.account.constant.ExceptionConstant;
import com.justedlev.account.model.params.AccountFilterParams;
import com.justedlev.account.model.request.AccountRequest;
import com.justedlev.account.model.request.UpdateAccountModeRequest;
import com.justedlev.account.model.response.AccountResponse;
import com.justedlev.account.repository.custom.filter.AccountFilter;
import com.justedlev.account.service.AccountService;
import com.justedlev.common.model.request.PaginationRequest;
import com.justedlev.common.model.response.PageResponse;
import com.justedlev.common.model.response.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountComponent accountComponent;
    private final AccountMapper accountMapper;
    private final ReportMapper reportMapper;
    private final AccountModeComponent accountModeComponent;
    private final ModelMapper modelMapper;
    private final NotificationComponent notificationComponent;

    @Override
    public PageResponse<AccountResponse> getPage(PaginationRequest request) {
        var page = accountComponent.getPage(request.toPegeable());

        return PageResponse.from(page, accountMapper::map);
    }

    @Override
    @Transactional(timeout = 120, isolation = Isolation.READ_UNCOMMITTED)
    public PageResponse<AccountResponse> getPageByFilter(AccountFilterParams params, PaginationRequest pagination) {
        var filter = modelMapper.typeMap(AccountFilterParams.class, AccountFilter.class)
                .addMapping(AccountFilterParams::getQ, AccountFilter::setSearchText)
                .map(params);
        var page = accountComponent.getPageByFilter(filter, pagination.toPegeable());

        return PageResponse.from(page, accountMapper::map);
    }

    @Override
    public AccountResponse getByEmail(String email) {
        var filter = AccountFilter.builder()
//                .emails(Set.of(email))
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
        notificationComponent.sendConfirmationEmail(saved);

        return accountMapper.map(saved);
    }
}
