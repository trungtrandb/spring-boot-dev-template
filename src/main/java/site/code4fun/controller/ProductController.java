package site.code4fun.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.dto.ProductDTO;
import site.code4fun.model.dto.QuestionDTO;
import site.code4fun.model.dto.ReviewDTO;
import site.code4fun.model.mapper.ProductMapper;
import site.code4fun.model.mapper.QuestionMapper;
import site.code4fun.model.mapper.ReviewMapper;
import site.code4fun.service.ProductService;

import java.util.List;
import java.util.Map;

import static site.code4fun.constant.CacheName.*;

@RestController
@RequestMapping(AppEndpoints.PRODUCTS_ENDPOINT)
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Lazy
public class ProductController {
    private final ProductService service;
    private final ProductMapper mapper;
    private final ReviewMapper reviewMapper;
    private final QuestionMapper questionMapper;

    @Cacheable({PRODUCT_PAGING})
    @GetMapping
    public Page<ProductDTO> getAllPaging(@RequestParam Map<String, String> mapRequest) {
        return service.getPaging(mapRequest).map(mapper::entityToDto);
    }

    @Cacheable({PRODUCT_POPULAR})
    @GetMapping("/popular")
    public Page<ProductDTO> getPopular(@RequestParam Map<String, String> mapRequest) {
        return service.getPaging(mapRequest).map(mapper::entityToDto);
    }

    @Cacheable({PRODUCT_BEST_SELLING})
    @GetMapping("/best-selling")
    public Page<ProductDTO> getBestSelling(@RequestParam Map<String, String> mapRequest) {
        return service.getPaging(mapRequest).map(mapper::entityToDto);
    }

    @Cacheable(value = "product-detail", key = "#id")
    @GetMapping("/{id}")
    public ProductDTO getById(@PathVariable long id){
        return mapper.entityToDto(service.getById(id));
    }

    @GetMapping("/in_wishlist/{id}")
    public boolean checkProductIsInWishList(@PathVariable long id){
        return false;
    }

    @GetMapping("/related/{id}")
    public List<ProductDTO> getRelatedProduct(@PathVariable long id){
        return service.getRelatedProducts(id).stream().map(mapper::entityToDto).toList();
    }

    @GetMapping("/{id}/reviews") // ProductId
    public Page<ReviewDTO> geProductReviews(@PathVariable String id, @RequestParam Map<String, String> mapRequest){
        mapRequest.put("productId", id);
        return service.getReviewsByProductId(mapRequest).map(reviewMapper::entityToDto);
    }

    @PostMapping("/reviews")
    @Transactional
    public ResponseEntity<ReviewDTO> createProductReviews(@RequestBody ReviewDTO dto){
        var req = reviewMapper.requestToEntity(dto);

        return ResponseEntity.ok(reviewMapper.entityToDto(service.createReview(req)));
    }

    @GetMapping("/reviews/{id}") // Review id
    public List<ProductDTO> getReviewById(@PathVariable long id){
        return service.getRelatedProducts(id).stream().map(mapper::entityToDto).toList();
    }

    @GetMapping("/{id}/questions") // Product id
    public Page<QuestionDTO> getAllQuestion(@PathVariable String id, @RequestParam Map<String, String> mapRequest){
        mapRequest.put("productId", id);
        return service.getQuestionByProductId(mapRequest).map(questionMapper::entityToDto);
    }

    @Transactional
    @PostMapping("/{id}/questions") // ProductId
    public ResponseEntity<QuestionDTO> createQuestion(@PathVariable long id, @RequestBody QuestionDTO dto){
        var req = questionMapper.requestToEntity(dto);

        return ResponseEntity.ok(questionMapper.entityToDto(service.createQuestion(req)));
    }
}
