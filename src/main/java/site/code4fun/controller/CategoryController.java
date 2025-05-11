package site.code4fun.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.dto.CategoryDTO;
import site.code4fun.model.mapper.CategoryMapper;
import site.code4fun.service.CategoryService;

import java.util.Map;

import static site.code4fun.constant.CacheName.CATEGORY_DETAIL;
import static site.code4fun.constant.CacheName.CATEGORY_PAGING;

@RestController
@RequestMapping(AppEndpoints.CATEGORIES_ENDPOINT)
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Lazy
public class CategoryController {
    private final CategoryService service;
    private final CategoryMapper mapper;

    @Cacheable(CATEGORY_PAGING)
    @GetMapping
    public Page<CategoryDTO> getAllPaging(@RequestParam Map<String, String> mapRequest) {
        return service.getPublicPaging(mapRequest).map(mapper::entityToDto);
    }

    @Cacheable(value = CATEGORY_DETAIL, key = "#id")
    @GetMapping("/{id}")
    public CategoryDTO getById(@PathVariable long id){
        return mapper.entityToDto(service.getById(id));
    }
}
