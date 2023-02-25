package com.justedlev.account.repository;

import com.justedlev.account.repository.custom.AccountCustomRepository;
import com.justedlev.account.repository.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account>, AccountCustomRepository {
    List<Account> findByNicknameIn(Collection<String> nicknames);

    List<Account> findByContactsEmailIn(Collection<String> emails);

    boolean existsByNickname(String nickname);
}
