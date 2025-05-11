package site.code4fun.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BaseService<T,I> {
    Page<T> getPaging(Map<String, String> mapRequestParam);
    T create(T entity);
    T replace(I id, T entity);
    T update(I id, T entity);
    List<T> getAll();
    void delete(I id);
    void deleteByIds(Collection<I> ids);
    T getById(I id);
    ByteArrayResource export();
}

