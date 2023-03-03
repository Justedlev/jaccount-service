package com.justedlev.account.repository.custom.filter;

import com.justedlev.account.repository.entity.Contact;
import com.justedlev.account.repository.entity.Contact_;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactFilter implements Filter<Contact> {
    private Collection<String> emails;
    private String searchText;

    @Override
    public List<Predicate> toPredicates(CriteriaBuilder cb, Path<Contact> path) {
        List<Predicate> predicates = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(getEmails())){
            predicates.add(path.get(Contact_.email).in(getEmails()));
        }

        return predicates;
    }

    @Override
    public Optional<Predicate> search(CriteriaBuilder cb, Path<Contact> path) {
        return Optional.ofNullable(searchText)
                .filter(StringUtils::isNotBlank)
                .map(String::toLowerCase)
                .map(String::strip)
                .map(q -> q.replaceAll("\\s+", " "))
                .map(q -> "%" + q + "%")
                .map(q -> cb.or(
                        cb.like(cb.lower(path.get(Contact_.email)), q)
                ));
    }
}
