package site.code4fun.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import site.code4fun.constant.SearchOperator;
import site.code4fun.model.PromptEntity;
import site.code4fun.model.dto.SearchCriteria;
import site.code4fun.repository.SearchSpecification;
import site.code4fun.repository.jpa.PromptRepository;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
public class PromptService extends AbstractBaseService<PromptEntity, Long> {

    @Getter(AccessLevel.PROTECTED)
    private final PromptRepository repository;

    @Override
    protected Specification<PromptEntity> createSpecification(List<SearchCriteria> criteriaList, String queryString) {
        Specification<PromptEntity> spec = null;

        if (isNotBlank(queryString)) {
            spec = new SearchSpecification<>(new SearchCriteria("name", SearchOperator.EQUAL, queryString));
            spec = spec.or(new SearchSpecification<>(new SearchCriteria("content", SearchOperator.EQUAL, queryString)));
        }

        for (SearchCriteria criteria : criteriaList) {
            spec = (spec == null) ? new SearchSpecification<>(criteria) : spec.and(new SearchSpecification<>(criteria));
        }

        return spec;
    }

}
