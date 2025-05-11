package site.code4fun.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import site.code4fun.constant.SearchOperator;
import site.code4fun.model.EventEntity;
import site.code4fun.model.LeaveTypeEntity;
import site.code4fun.model.dto.SearchCriteria;
import site.code4fun.repository.SearchSpecification;
import site.code4fun.repository.jpa.EventRepository;
import site.code4fun.repository.jpa.LeaveTypeRepository;

import java.util.Collection;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static site.code4fun.constant.AppConstants.SEARCH_KEY;

@Service
@RequiredArgsConstructor
@Lazy
@Getter
public class LeaveTypeService extends AbstractBaseService<LeaveTypeEntity, Long> {
    private final LeaveTypeRepository repository;
    private final EventRepository eventRepository;

    public Page<EventEntity> getAllHoliday(Map<String, String> mapRequestParam) {
        Pageable pageReq = buildPageRequest(mapRequestParam);
        String queryString = mapRequestParam.get(SEARCH_KEY);

        if (isNotBlank(queryString)) {
            Specification<EventEntity> spec = new SearchSpecification<>(new SearchCriteria("name", SearchOperator.EQUAL, queryString));
            return eventRepository.findAll(spec, pageReq);
        }

        return eventRepository.findAll(pageReq);
    }

    public void deleteHolidayById(Long id) {
        eventRepository.findById(id).ifPresent(eventRepository::delete);
    }

    public void deleteHolidayByIds(Collection<Long> ids) {
        ids.forEach(this::deleteHolidayById);
    }

    public EventEntity createHoliday(EventEntity request) {
       return eventRepository.save(request);
    }
}
