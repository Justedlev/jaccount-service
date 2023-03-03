package com.justedlev.account.repository.custom;

import com.justedlev.account.repository.custom.filter.AccountFilter;
import com.justedlev.account.repository.entity.Account;

public interface AccountFilterableRepository extends FilterableRepository<Account, AccountFilter> {
}
