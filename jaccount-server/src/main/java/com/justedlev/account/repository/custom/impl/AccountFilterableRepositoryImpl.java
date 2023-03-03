package com.justedlev.account.repository.custom.impl;

import com.justedlev.account.repository.custom.AccountFilterableRepository;
import com.justedlev.account.repository.custom.filter.AccountFilter;
import com.justedlev.account.repository.entity.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AccountFilterableRepositoryImpl implements AccountFilterableRepository {
    @PersistenceContext
    private final EntityManager em;

    @Override
    public List<Account> findByFilter(@NonNull AccountFilter filter) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Account.class);
        var root = cq.from(Account.class);
        root.fetch(Account_.contacts, JoinType.LEFT);
        var predicateList = filter.toPredicates(cb, root);
        applyPredicates(cq, predicateList);

        return em.createQuery(cq).getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Account> findByFilter(@NonNull AccountFilter filter, @NonNull Pageable pageable) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Account.class);
        var root = cq.from(Account.class);
        var contacts = (Join<Account, Contact>) root.fetch(Account_.contacts, JoinType.LEFT);
        var phoneNumber = (Join<Contact, PhoneNumber>) contacts.fetch(Contact_.phoneNumber, JoinType.LEFT);
        var predicates = applyPredicates(filter, cb, cq, root, contacts, phoneNumber);
        var content = applyPageable(pageable, cb, cq, root).getResultList();

        return PageableExecutionUtils.getPage(content, pageable, () -> executeCountQuery(predicates));
    }

    private Predicate[] applyPredicates(AccountFilter filter,
                                        CriteriaBuilder cb,
                                        CriteriaQuery<Account> cq,
                                        Root<Account> root,
                                        Join<Account, Contact> contacts,
                                        Join<Contact, PhoneNumber> phoneNumber) {
        var predicateList = filter.toPredicates(cb, root);
        createSearchPredicate(filter, cb, root, contacts, phoneNumber)
                .ifPresent(predicateList::add);

        return applyPredicates(cq, predicateList);
    }

    private Predicate[] applyPredicates(CriteriaQuery<?> cq,
                                        List<Predicate> predicateList) {
        var predicateArray = predicateList.toArray(Predicate[]::new);
        Optional.ofNullable(predicateArray)
                .filter(ArrayUtils::isNotEmpty)
                .ifPresent(cq::where);

        return predicateArray;
    }

    private TypedQuery<Account> applyPageable(Pageable pageable, CriteriaBuilder cb,
                                              CriteriaQuery<Account> cq, Root<Account> root) {
        Optional.ofNullable(pageable)
                .map(Pageable::getSort)
                .filter(Sort::isSorted)
                .map(current -> QueryUtils.toOrders(pageable.getSort(), root, cb))
                .ifPresent(cq::orderBy);
        var query = em.createQuery(cq);
        Optional.ofNullable(pageable)
                .filter(Pageable::isPaged)
                .ifPresent(current -> query.setFirstResult((int) current.getOffset())
                        .setMaxResults(current.getPageSize()));

        return query;
    }

    private long executeCountQuery(Predicate[] predicates) {
        return em.createQuery(createCountQuery(predicates))
                .getSingleResult();
    }

    private CriteriaQuery<Long> createCountQuery(Predicate[] predicates) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(Account.class);
        root.join(Account_.contacts, JoinType.LEFT)
                .join(Contact_.phoneNumber, JoinType.LEFT);
        Optional.ofNullable(predicates)
                .filter(ArrayUtils::isNotEmpty)
                .ifPresent(cq::where);

        return cq.select(cb.count(root));
    }

    private Optional<Predicate> createSearchPredicate(AccountFilter filter,
                                                      CriteriaBuilder cb,
                                                      Path<Account> root,
                                                      Join<Account, Contact> contacts,
                                                      Join<Contact, PhoneNumber> phoneNumber) {
        return Optional.ofNullable(filter.getSearchText())
                .filter(StringUtils::isNotBlank)
                .map(String::toLowerCase)
                .map(String::strip)
                .map(q -> q.replaceAll("\\s+", " "))
                .map(q -> "%" + q + "%")
                .map(q -> cb.or(
                        cb.like(cb.lower(root.get(Account_.nickname)), q),
                        cb.like(cb.lower(root.get(Account_.firstName)), q),
                        cb.like(cb.lower(root.get(Account_.lastName)), q),
                        cb.like(cb.lower(contacts.get(Contact_.email)), q),
                        cb.like(cb.lower(phoneNumber.get(PhoneNumber_.national).as(String.class)), q),
                        cb.like(cb.lower(phoneNumber.get(PhoneNumber_.international)), q),
                        cb.like(cb.lower(phoneNumber.get(PhoneNumber_.countryCode).as(String.class)), q),
                        cb.like(cb.lower(phoneNumber.get(PhoneNumber_.regionCode)), q)
                ));
    }
}
