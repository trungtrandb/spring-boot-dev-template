package site.code4fun.service;


import io.jsonwebtoken.lang.Collections;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import site.code4fun.ApplicationProperties;
import site.code4fun.constant.Status;
import site.code4fun.exception.ServiceException;
import site.code4fun.model.*;
import site.code4fun.model.dto.SearchCriteria;
import site.code4fun.repository.SearchSpecification;
import site.code4fun.repository.jpa.*;
import site.code4fun.util.QRCodeUtils;
import site.code4fun.util.SecurityUtils;
import site.code4fun.util.UrlParserUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Lazy
public class ProductService extends AbstractBaseService<Product, Long> {
    @Getter(AccessLevel.PROTECTED)
    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final ApplicationProperties properties;
    private final TagRepository tagRepository;
    private final ReviewRepository reviewRepository;
    private final QuestionRepository questionRepository;
    @Override
    public Product create(Product product) {
        if (product.getId() != null) {
            Product existingProduct = getById(product.getId());
            product.setInventories(existingProduct.getInventories());
        }

        handleCategories(product);
        handleTags(product);
        handleThumbnail(product);

        return getRepository().save(product);
    }

    private void handleCategories(Product product) {
        if (product.getCategories() != null) {
            Set<CategoryEntity> categories = new HashSet<>(product.getCategories());
            product.setCategories(new HashSet<>());
            categories.forEach(category -> {
                if (category.getId() != null) {
                    product.addCategory(categoryRepository.getReferenceById(category.getId()));
                } else {
                    category.setStatus(Status.ACTIVE);
                    product.addCategory(categoryRepository.save(category));
                }
            });
        }
    }

    private void handleThumbnail(Product product){
        if (!Collections.isEmpty(product.getFiles()))
            product.setThumbnail(product.getFiles().iterator().next().getLink());
    }

    private void handleTags(Product product) {
        if (product.getTags() != null) {
            Set<TagEntity> tags = new HashSet<>(product.getTags());
            product.setTags(new HashSet<>());
            tags.forEach(tag -> {
                if (tag.getId() != null) {
                    product.addTag(tagRepository.getReferenceById(tag.getId()));
                } else {
                    product.addTag(tagRepository.save(tag));
                }
            });
        }
    }


    public List<Product> findAllById(Collection<Long> ids){
        return getRepository().findAllById(ids);
    }

    public List<Product> getRelatedProducts(long id) {
        List<Long> catIds = getById(id).getCategories().stream().map(CategoryEntity::getId).toList();
        return getRepository().findAllByCategories_IdInOrderByIdDesc(catIds, Limit.of(4)).stream().filter(p -> p.getId() != id).toList();
    }

    public Product addToInventory(Long productId, InventoryEntity request) {
        Product p = getById(productId);

        InventoryEntity inv = new InventoryEntity();
        inv.setCost(request.getCost());
        inv.setContent(request.getContent());
        inv.setQuantity(request.getQuantity());
        inv.setSupplier(request.getSupplier());
        p.addInventoryEntity(inv);
        p.setQuantity(p.getQuantity() + request.getQuantity());
        return p;
    }

    @Override
    public Page<Product> getPaging(Map<String, String> mapRequestParam) {
        if ("cost".equalsIgnoreCase(mapRequestParam.get("sort"))){ // sort by inventory cost
            mapRequestParam.remove("sort");
            if (mapRequestParam.get("sortDir").equalsIgnoreCase("asc")){
                return getRepository().findPageByInventoryCost(buildPageRequest(mapRequestParam));
            }else {
                return getRepository().findPageByInventoryCostDesc(buildPageRequest(mapRequestParam));
            }
        }else{ // normal paging
            return super.getPaging(mapRequestParam);
        }
    }

    public void decreaseQuantity(Long productId, int reduceNumber){
        Product product = getById(productId);
        if (product.getQuantity() < reduceNumber){
            throw new ServiceException("Product " + product.getName() + " out of stock");
        }
        product.setQuantity(product.getQuantity() - reduceNumber);
        getRepository().save(product);
    }

    @SneakyThrows
    public ByteArrayResource exportProductQR(Long id) {
        byte[] res = QRCodeUtils.getQRCodeImage(properties.getAdminDomain() + "/product-edit/" + id, 300, 300);
        return new ByteArrayResource(res);
    }

    public ReviewEntity createReview(ReviewEntity entity){
        entity.setUser(SecurityUtils.getUser());
        return reviewRepository.save(entity);
    }

    public Page<ReviewEntity> getReviewsByProductId(Map<String, String> searchRequest){
        Pageable pageRequest = buildPageRequest(searchRequest);
        List<SearchCriteria> lstSearch = UrlParserUtils.parserQueryString(searchRequest, ReviewEntity.class);
        Specification<ReviewEntity> spec = null;
        for (SearchCriteria criteria : lstSearch) {
            spec = (spec == null) ? new SearchSpecification<>(criteria) : spec.and(new SearchSpecification<>(criteria));
        }
        return null != spec ? reviewRepository.findAll(spec, pageRequest) : reviewRepository.findAll(pageRequest);
    }

    public QuestionEntity createQuestion(QuestionEntity entity){
        entity.setUser(SecurityUtils.getUser());
        return questionRepository.save(entity);
    }

    public Page<QuestionEntity> getQuestionByProductId(Map<String, String> searchRequest){
        Pageable pageRequest = buildPageRequest(searchRequest);
        List<SearchCriteria> lstSearch = UrlParserUtils.parserQueryString(searchRequest, QuestionEntity.class);
        Specification<QuestionEntity> spec = null;
        for (SearchCriteria criteria : lstSearch) {
            spec = (spec == null) ? new SearchSpecification<>(criteria) : spec.and(new SearchSpecification<>(criteria));
        }
        return null != spec ? questionRepository.findAll(spec, pageRequest) : questionRepository.findAll(pageRequest);
    }
}
