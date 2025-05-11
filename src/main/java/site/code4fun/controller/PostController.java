package site.code4fun.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.dto.PostLiteDTO;
import site.code4fun.model.mapper.PostMapper;
import site.code4fun.service.PostService;


@RestController
@RequestMapping(AppEndpoints.POSTS_ENDPOINT)
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Lazy
public class PostController {
    private final PostService service;
    private final PostMapper mapper;

//    @GetMapping
//    @Transactional
//    public Page<PostTmp> getAllPaging(@RequestParam Map<String, String> mapRequest) {
//        return service.getPaging(mapRequest).map(mapper::entityToTmp);
//    }

    @GetMapping(value = "/blogs", produces = {"application/json"})
    public Page<PostLiteDTO> getAllBlogPaging(@RequestParam(required = false, defaultValue = "0") int page) {
        return service.getPublicPaging(page).map(mapper::postToPostLiteDTO);
    }

    @GetMapping(value = "/blogs-sitemap", produces = {"application/json"}) // Don't need optimize, get all for gen sitemap
    public Page<PostLiteDTO> getAllBlogAllForSiteMap() {
        return service.getForSiteMap().map(mapper::postToPostLiteDTO);
    }

    @GetMapping("/blogs/{slug}")
    public PostLiteDTO getBlogById(@PathVariable String slug){
        return mapper.postToPostLiteDTO(service.getPostBySLug(slug));
    }
//
//    @GetMapping("/{id}")
//    public PostTmp getById(@PathVariable Long id){
//        return mapper.entityToTmp(service.getById(id));
//    }
//
//
//    @GetMapping("/refund-policies")
//    public Page<PostTmp> getRefundPolicies(@RequestParam Map<String, String> mapRequest) {
//        mapRequest.put("type", PostType.REFUND_POLICY.toString());
//       return service.getPaging(mapRequest).map(mapper::entityToTmp);
//    }
//
//    @GetMapping("/faqs")
//    public Page<PostTmp> faqs(@RequestParam Map<String, String> mapRequest) {
//        mapRequest.put("type", PostType.FAQ.toString());
//        return service.getPaging(mapRequest).map(mapper::entityToTmp);
//    }
}
