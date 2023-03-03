package com.justedlev.account.repository.custom;

import com.justedlev.account.repository.custom.filter.Filter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FilterableRepository<E, F extends Filter<E>> {
    List<E> findByFilter(F filter);

    Page<E> findByFilter(F filter, Pageable pageable);
}
