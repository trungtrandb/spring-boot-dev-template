package site.code4fun.service;

import static site.code4fun.constant.AppConstants.DEFAULT_PAGE_SIZE;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import site.code4fun.constant.Status;
import site.code4fun.model.CategoryEntity;
import site.code4fun.repository.jpa.CategoryRepository;
import site.code4fun.repository.jpa.ProductRepository;

import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class CategoryService extends AbstractBaseService<CategoryEntity , Long> {

    private final CategoryRepository repository;
    private final ProductRepository productRepository;

    @Override
    public CategoryEntity  create(CategoryEntity  entity) {
        if (entity.getStatus() == null){
            entity.setStatus(Status.ACTIVE);
        }
        return getRepository().save(entity);
    }

    @Override
    public void delete(Long id) {
        CategoryEntity  category = getById(id);
        productRepository.findAllByCategories_IdIn(Collections.singletonList(id)).forEach(product -> product.removeCategory(category)); // TODO need many query to delete join table
        getRepository().delete(category);
    }


    public Page<CategoryEntity> getPublicPaging(Map<String, String> mapRequestParam) {
//        Pageable pageReq = buildPageRequest(mapRequestParam);
        int page = enSureNotNullPage(mapRequestParam.get("page"));
        Pageable pageReq = PageRequest.of(page, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));

//        List<SearchCriteria> lstSearch = UrlParserUtils.parserQueryString(mapRequestParam, Post.class);
//        Specification<Post> spec = createSpecification(lstSearch, mapRequestParam.get(SEARCH_KEY));

        return getRepository().findAllByStatusEquals(Status.ACTIVE, pageReq);
    }
}
