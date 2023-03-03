package com.justedlev.account.repository.custom.impl;

import com.justedlev.account.repository.custom.ContactFilterableRepository;
import com.justedlev.account.repository.custom.filter.ContactFilter;
import com.justedlev.account.repository.entity.Account;
import com.justedlev.account.repository.entity.Contact;
import com.justedlev.account.repository.entity.Contact_;
import com.justedlev.account.repository.entity.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
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

@Repository
@RequiredArgsConstructor
public class ContactFilterableRepositoryImpl implements ContactFilterableRepository {
    @PersistenceContext
    private final EntityManager em;

    @Override
    public List<Contact> findByFilter(ContactFilter filter) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Contact.class);
        var root = cq.from(Contact.class);
        root.fetch(Contact_.account, JoinType.LEFT);
        var predicateList = filter.toPredicates(cb, root);
        applyPredicates(cq, predicateList);

        return em.createQuery(cq).getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Contact> findByFilter(ContactFilter filter, Pageable pageable) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Contact.class);
        var root = cq.from(Contact.class);
        var account = (Join<Contact, Account>) root.fetch(Contact_.account, JoinType.LEFT);
        var phoneNumber = (Join<Contact, PhoneNumber>) root.fetch(Contact_.phoneNumber, JoinType.LEFT);
        var predicates = applyPredicates(filter, cb, cq, root, account, phoneNumber);
        var content = applyPageable(pageable, cb, cq, root).getResultList();

        return PageableExecutionUtils.getPage(content, pageable, () -> executeCountQuery(predicates));
    }

    private TypedQuery<Contact> applyPageable(Pageable pageable, CriteriaBuilder cb,
                                              CriteriaQuery<Contact> cq, Root<Contact> root) {
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
        var root = cq.from(Contact.class);
        root.join(Contact_.account, JoinType.LEFT);
        root.join(Contact_.phoneNumber, JoinType.LEFT);
        Optional.ofNullable(predicates)
                .filter(ArrayUtils::isNotEmpty)
                .ifPresent(cq::where);

        return cq.select(cb.count(root));
    }

    private Predicate[] applyPredicates(ContactFilter filter,
                                        CriteriaBuilder cb,
                                        CriteriaQuery<Contact> cq,
                                        Root<Contact> root,
                                        Join<Contact, Account> account,
                                        Join<Contact, PhoneNumber> phoneNumber) {
        var predicateList = filter.toPredicates(cb, root);

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
}
