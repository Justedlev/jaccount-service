package com.justedlev.account.repository.custom.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.Optional;

public interface Filter<E> {
    List<Predicate> toPredicates(CriteriaBuilder cb, Path<E> path);

    Optional<Predicate> search(CriteriaBuilder cb, Path<E> path);

}
