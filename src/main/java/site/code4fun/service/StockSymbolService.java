package site.code4fun.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import site.code4fun.constant.SearchOperator;
import site.code4fun.model.StockSymbolEntity;
import site.code4fun.model.dto.SearchCriteria;
import site.code4fun.repository.SearchSpecification;
import site.code4fun.repository.jpa.StockSymbolRepository;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
@Getter(AccessLevel.PROTECTED)
public class StockSymbolService extends AbstractBaseService<StockSymbolEntity, Integer> {

    private final StockSymbolRepository repository;

    protected Specification<StockSymbolEntity> createSpecification(List<SearchCriteria> criteriaList, String queryString) {
        Specification<StockSymbolEntity> spec = null;

        if (isNotBlank(queryString)) {
            spec = new SearchSpecification<>(new SearchCriteria("organName", SearchOperator.EQUAL, queryString));
            spec = spec.or(new SearchSpecification<>(new SearchCriteria("symbol", SearchOperator.EQUAL, queryString)));
        }

        for (SearchCriteria criteria : criteriaList) {
            spec = (spec == null) ? new SearchSpecification<>(criteria) : spec.and(new SearchSpecification<>(criteria));
        }

        return spec;
    }

}
