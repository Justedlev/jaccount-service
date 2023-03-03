package com.justedlev.account.repository.custom.filter;

import com.justedlev.account.enumeration.AccountStatusCode;
import com.justedlev.account.enumeration.ModeType;
import com.justedlev.account.repository.entity.Account;
import com.justedlev.account.repository.entity.Account_;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.sql.Timestamp;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountFilter implements Filter<Account> {
    private Collection<UUID> ids;
    private Collection<String> nicknames;
    private Collection<AccountStatusCode> statuses;
    private Collection<ModeType> modes;
    private Collection<String> activationCodes;
    private Timestamp modeAtFrom;
    private Timestamp modeAtTo;
    private String searchText;

    @Override
    public List<Predicate> toPredicates(CriteriaBuilder cb, Path<Account> path) {
        List<Predicate> predicates = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(getIds())) {
            predicates.add(path.get(Account_.id).in(getIds()));
        }

        if (CollectionUtils.isNotEmpty(getNicknames())) {
            predicates.add(path.get(Account_.nickname).in(getNicknames()));
        }

        if (CollectionUtils.isNotEmpty(getModes())) {
            predicates.add(path.get(Account_.mode).in(getModes()));
        }

        if (ObjectUtils.isNotEmpty(getModeAtFrom())) {
            predicates.add(cb.greaterThanOrEqualTo(path.get(Account_.modeAt), getModeAtFrom()));
        }

        if (ObjectUtils.isNotEmpty(getModeAtTo())) {
            predicates.add(cb.lessThanOrEqualTo(path.get(Account_.modeAt), getModeAtTo()));
        }

        if (CollectionUtils.isNotEmpty(getStatuses())) {
            predicates.add(path.get(Account_.status).in(getStatuses()));
        }

        if (CollectionUtils.isNotEmpty(getActivationCodes())) {
            predicates.add(path.get(Account_.activationCode).in(getActivationCodes()));
        }

        return predicates;
    }

    @Override
    public Optional<Predicate> search(CriteriaBuilder cb, Path<Account> path) {
        return Optional.ofNullable(searchText)
                .filter(StringUtils::isNotBlank)
                .map(String::toLowerCase)
                .map(String::strip)
                .map(q -> q.replaceAll("\\s+", " "))
                .map(q -> "%" + q + "%")
                .map(q -> cb.or(
                        cb.like(cb.lower(path.get(Account_.nickname)), q),
                        cb.like(cb.lower(path.get(Account_.firstName)), q),
                        cb.like(cb.lower(path.get(Account_.lastName)), q)
                ));
    }
}
