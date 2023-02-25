package com.justedlev.account.repository.custom.impl;

import com.justedlev.account.repository.custom.AccountCustomRepository;
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
public class AccountCustomRepositoryImpl implements AccountCustomRepository {
    @PersistenceContext
    private final EntityManager em;

    @Override
    public List<Account> findByFilter(@NonNull AccountFilter filter) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Account.class);
        var root = cq.from(Account.class);
        var predicateList = filter.apply(cb, root);
        applyPredicates(cq, predicateList);

        return em.createQuery(cq).getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Account> findByFilter(@NonNull AccountFilter filter, @NonNull Pageable pageable) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Account.class);
        var root = cq.from(Account.class);
        var contacts = (Join<Account, Contact>) root.fetch(Account_.contacts);
        var phoneNumber = (Join<Contact, PhoneNumber>) contacts.fetch(Contact_.phoneNumber);
        var predicates = applyPredicates(filter, cb, cq, root, contacts, phoneNumber);
        var content = applyPageable(pageable, cb, cq, root)
                .getResultList();

        return PageableExecutionUtils.getPage(content, pageable, () -> executeCountQuery(predicates));
    }

    private Predicate[] applyPredicates(AccountFilter filter,
                                        CriteriaBuilder cb,
                                        CriteriaQuery<Account> cq,
                                        Root<Account> root,
                                        Join<Account, Contact> contacts,
                                        Join<Contact, PhoneNumber> phoneNumber) {
        var predicateList = filter.apply(cb, root);
        createSearchPredicate(filter.getSearchText(), cb, root, contacts, phoneNumber)
                .ifPresent(predicateList::add);

        return applyPredicates(cq, predicateList);
    }

    private Predicate[] applyPredicates(CriteriaQuery<Account> cq,
                                        List<Predicate> predicateList) {
        var predicateArray = predicateList.toArray(Predicate[]::new);

        if (ArrayUtils.isNotEmpty(predicateArray)) {
            cq.where(predicateArray);
        }

        return predicateArray;
    }

    private TypedQuery<Account> applyPageable(Pageable pageable, CriteriaBuilder cb,
                                              CriteriaQuery<Account> cq, Root<Account> root) {
        var query = em.createQuery(cq);

        if (pageable.isPaged()) {
            applyOrders(pageable.getSort(), cb, cq, root);
            query.setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize());
        }

        return query;
    }

    private void applyOrders(Sort sort, CriteriaBuilder cb, CriteriaQuery<Account> cq, Root<Account> root) {
        if (sort.isSorted()) {
            var orders = QueryUtils.toOrders(sort, root, cb);
            cq.orderBy(orders);
        }
    }

    private long executeCountQuery(Predicate... predicates) {
        return em.createQuery(createCountQuery(predicates))
                .getSingleResult();
    }

    private CriteriaQuery<Long> createCountQuery(Predicate... predicates) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(Account.class);
        root.join(Account_.contacts).join(Contact_.phoneNumber);

        if (ArrayUtils.isNotEmpty(predicates)) {
            cq.where(predicates);
        }

        return cq.select(cb.count(root));
    }

    private Optional<Predicate> createSearchPredicate(String searchText,
                                                      CriteriaBuilder cb,
                                                      Path<Account> root,
                                                      Join<Account, Contact> contacts,
                                                      Join<Contact, PhoneNumber> phoneNumber) {
        return Optional.ofNullable(searchText)
                .filter(StringUtils::isNotBlank)
                .map(String::toLowerCase)
                .map(q -> q.replaceAll("\\s{2}", " "))
                .map(q -> "%" + q + "%")
                .map(q -> cb.or(
                        cb.like(cb.lower(root.get(Account_.nickname)), q),
                        cb.like(cb.lower(root.get(Account_.firstName)), q),
                        cb.like(cb.lower(root.get(Account_.lastName)), q),
                        cb.like(cb.lower(contacts.get(Contact_.email)), q),
                        cb.like(cb.lower(phoneNumber.get(PhoneNumber_.national).as(String.class)), q),
                        cb.like(cb.lower(phoneNumber.get(PhoneNumber_.international).as(String.class)), q),
                        cb.like(cb.lower(phoneNumber.get(PhoneNumber_.countryCode).as(String.class)), q),
                        cb.like(cb.lower(phoneNumber.get(PhoneNumber_.regionCode).as(String.class)), q)
                ));
    }
}
