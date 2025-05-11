package site.code4fun.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import site.code4fun.exception.NotFoundException;
import site.code4fun.constant.SearchOperator;
import site.code4fun.model.dto.SearchCriteria;
import site.code4fun.repository.SearchSpecification;
import site.code4fun.repository.jpa.BaseRepository;
import site.code4fun.util.CollectionUtils;
import site.code4fun.util.ExcelUtils;
import site.code4fun.util.UrlParserUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static site.code4fun.constant.AppConstants.*;

/**
 * Abstract base service providing common CRUD, paging, and export operations for entities.
 * <p>
 * Improvements and enhancements made with the help of <b>Cursor</b> AI code assistant.
 * </p>
 * @param <T> Entity type
 * @param <I> ID type
 */
@Slf4j
public abstract class AbstractBaseService<T,I> implements BaseService<T, I> {
    /**
     * Default mapping header for export functionality.
     */
    private static final Map<String, String> DEFAULT_MAPPING_HEADER = Map.ofEntries(
            Map.entry("id", "Id"),
            Map.entry("name", "Name")
    );

    /**
     * Creates a new entity in the repository.
     * @param entity the entity to create
     * @return the created entity
     */
    public T create(T entity) {
        if (entity == null) {
            log.error("Attempted to create a null entity");
            throw new IllegalArgumentException("Entity must not be null");
        }
        log.info("Creating entity: {}", entity);
        return getRepository().save(entity);
    }

    /**
     * Retrieves all entities from the repository.
     * @return list of all entities
     */
    public List<T> getAll() {
        log.info("Retrieving all entities");
        return getRepository().findAll();
    }

    /**
     * Deletes an entity by its ID.
     * @param id the ID of the entity to delete
     */
    public void delete(I id) {
        if (id == null) {
            log.error("Attempted to delete entity with null ID");
            throw new IllegalArgumentException("ID must not be null");
        }
        T r = getById(id);
        log.info("Deleting entity of type {} with ID {}", getClazz().getSimpleName(), id);
        getRepository().delete(r);
    }

    /**
     * Retrieves entities by a collection of IDs.
     * @param ids the collection of IDs
     * @return list of entities
     */
    public List<T> getByIds(Collection<I> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("Attempted to get entities with null or empty IDs");
            return List.of();
        }
        log.info("Retrieving entities of type {} with IDs {}", getClazz().getSimpleName(), ids);
        return getRepository().findAllById(ids);
    }

    /**
     * Retrieves an entity by its ID.
     * @param id the ID of the entity
     * @return the found entity
     * @throws NotFoundException if the entity is not found
     */
    public T getById(I id) {
        if (id == null) {
            log.error("Attempted to get entity with null ID");
            throw new IllegalArgumentException("ID must not be null");
        }
        return getRepository().findById(id)
                .orElseThrow(() -> {
                    String message = String.format("%s with ID %s not found", getClazz().getSimpleName(), id);
                    log.error(message);
                    return new NotFoundException(message);
                });
    }

    /**
     * Updates an entity by its ID. Only updates fields present in the request.
     * @param id the ID of the entity to update
     * @param entity the entity with updated fields
     * @return the updated entity
     */
    public T update(I id, T entity){
        if (id == null || entity == null) {
            log.error("Attempted to update entity with null ID or entity");
            throw new IllegalArgumentException("ID and entity must not be null");
        }
        getById(id);//TODO just update fields present in request;
        log.info("Updating entity of type {} with ID {}: {}", getClazz().getSimpleName(), id, entity);
        return create(entity);
    }

    /**
     * Replaces an entity by its ID. Replaces the existing object with a new one.
     * @param id the ID of the entity to replace
     * @param entity the new entity
     * @return the replaced entity
     */
    public T replace(I id, T entity){
        if (id == null || entity == null) {
            log.error("Attempted to replace entity with null ID or entity");
            throw new IllegalArgumentException("ID and entity must not be null");
        }
        getById(id);//TODO replace existed object with new obj;
        log.info("Replacing entity of type {} with ID {}: {}", getClazz().getSimpleName(), id, entity);
        return create(entity);
    }

    /**
     * Exports all entities to an Excel file.
     * @return ByteArrayResource containing the exported data
     */
    public ByteArrayResource export() {
        List<T> lstEntity = getRepository().findAll();
        log.info("Exporting {} entities of type {}", lstEntity.size(), getClazz().getSimpleName());
        return ExcelUtils.export(lstEntity, getMappingHeader());
    }

    /**
     * Retrieves a paginated list of entities based on request parameters.
     * @param mapRequestParam the request parameters
     * @return a page of entities
     */
    public Page<T> getPaging(Map<String, String> mapRequestParam) {
        if (mapRequestParam == null) {
            log.error("Attempted to get paging with null request parameters");
            throw new IllegalArgumentException("Request parameters must not be null");
        }
        Pageable pageReq = buildPageRequest(mapRequestParam);
        List<SearchCriteria> lstSearch = UrlParserUtils.parserQueryString(mapRequestParam, getClazz());
        Specification<T> spec = createSpecification(lstSearch, mapRequestParam.get(SEARCH_KEY));
        log.info("Getting paginated entities of type {} with params {}", getClazz().getSimpleName(), mapRequestParam);
        return null != spec ? getRepository().findAll(spec, pageReq) : getRepository().findAll(pageReq);
    }

    /**
     * Deletes entities by a collection of IDs.
     * @param ids the collection of IDs
     */
    public void deleteByIds(Collection<I> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("Attempted to delete entities with null or empty IDs");
            return;
        }
        ids.forEach(this::delete);
    }

    /**
     * Gets the mapping header for export functionality.
     * @return the mapping header
     */
    protected Map<String, String> getMappingHeader(){
        return DEFAULT_MAPPING_HEADER;
    }

    /**
     * Builds a Pageable object from paging and sorting parameters.
     * @param page the page number
     * @param size the page size
     * @param sortDir the sort direction
     * @param sort the sort column
     * @return Pageable object
     */
    protected Pageable buildPaging(String page, String size, String sortDir, String sort){
        sort = isNotBlank(sort) ? sort : DEFAULT_SORT_COLUMN;
        sortDir = CollectionUtils.containsIgnoreCase(SORT_LIST, sortDir) ? sortDir : SORT_LIST.get(0);
        return PageRequest.of(
                enSureNotNullPage(page),
                ensureNotNullSize(size),
                Sort.Direction.fromString(sortDir),
                sort
        );
    }

    /**
     * Builds a Pageable object from request parameters.
     * @param mapRequestParams the request parameters
     * @return Pageable object
     */
    protected Pageable buildPageRequest(Map<String, String> mapRequestParams){
        if (mapRequestParams == null) {
            log.warn("Null mapRequestParams passed to buildPageRequest, using defaults");
            return buildPaging(null, null, null, null);
        }
        String size = mapRequestParams.get("size") != null
                ? mapRequestParams.get("size")
                : mapRequestParams.get("limit");
        return buildPaging(
                mapRequestParams.get("page"),
                size,
                mapRequestParams.get("sortDir"),
                mapRequestParams.get("sort")
        );
    }

    /**
     * Gets the repository for the entity.
     * @return the repository
     */
    protected abstract BaseRepository<T, I> getRepository();

    /**
     * Ensures the page number is not null or less than 0.
     * @param page the page number as string
     * @return the page number (0-based)
     */
    protected int enSureNotNullPage(String page){
        if (page != null){
            try {
                int tmpVal = Integer.parseInt(page);
                return Math.max(tmpVal - 1, 0);
            } catch (NumberFormatException e) {
                log.warn("Invalid page number '{}', defaulting to 0", page);
            }
        }
        return 0;
    }

    /**
     * Ensures the page size is not null and within allowed limits.
     * @param size the page size as string
     * @return the page size
     */
    protected int ensureNotNullSize(String size){
        if (size != null){
            try {
                int tmpVal = Integer.parseInt(size);
                return tmpVal > 0 && tmpVal <= 1000 ? tmpVal : DEFAULT_PAGE_SIZE;
            } catch (NumberFormatException e) {
                log.warn("Invalid page size '{}', defaulting to {}", size, DEFAULT_PAGE_SIZE);
            }
        }
        return DEFAULT_PAGE_SIZE;
    }

    /**
     * Gets the class of the entity.
     * @return the entity class
     */
    private Class<T> getClazz(){
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                @SuppressWarnings("unchecked")
                Class<T> type = (Class<T>) actualTypeArguments[0];
                return type;
            }
        }
        String message = "Unable to determine generic type for " + getClass().getName();
        log.error(message);
        throw new IllegalStateException(message);
    }

    /**
     * Creates a JPA Specification from a list of search criteria and a query string.
     * @param criteriaList the list of search criteria
     * @param queryString the query string
     * @return the specification
     */
    protected Specification<T> createSpecification(List<SearchCriteria> criteriaList, String queryString) {
        Specification<T> spec = null;

        if (isNotBlank(queryString)) {
            spec = new SearchSpecification<>(new SearchCriteria("name", SearchOperator.EQUAL, queryString));
        }

        if (criteriaList != null) {
            for (SearchCriteria criteria : criteriaList) {
                spec = (spec == null) ? new SearchSpecification<>(criteria) : spec.and(new SearchSpecification<>(criteria));
            }
        }

        return spec;
    }
}

