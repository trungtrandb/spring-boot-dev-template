package site.code4fun.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import site.code4fun.constant.PostType;
import site.code4fun.constant.SearchOperator;
import site.code4fun.constant.Status;
import site.code4fun.model.Post;
import site.code4fun.model.PostContent;
import site.code4fun.model.dto.SearchCriteria;
import site.code4fun.repository.SearchSpecification;
import site.code4fun.repository.jpa.PostRepository;
import site.code4fun.service.google.GoogleTranslateService;

import java.util.List;
import site.code4fun.util.RegexUtils;

import static org.apache.commons.lang3.StringUtils.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
@Getter
public class PostService extends AbstractBaseService<Post, Long> {
    private final PostRepository repository;
    private final GoogleTranslateService translateService;

    public Page<Post> getForSiteMap() {
        Pageable pageReq = PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "id")); // 6/12 is most adapt for grid 2,3,4 layout
        return getRepository().findAllByStatusEqualsAndTypeEquals(Status.ACTIVE, PostType.POST, pageReq);
    }

    @Override
    protected Specification<Post> createSpecification(List<SearchCriteria> criteriaList, String queryString) {
        Specification<Post> spec = null;

        if (isNotBlank(queryString)) {
            spec = new SearchSpecification<>(new SearchCriteria("postContents", SearchOperator.EQUAL, queryString));
        }

        for (SearchCriteria criteria : criteriaList) {
            spec = (spec == null) ? new SearchSpecification<>(criteria) : spec.and(new SearchSpecification<>(criteria));
        }

        return spec;
    }

    @Override
    public Post create(Post post){
        PostContent srcTrans = post.getPostContents().stream().filter(pc -> isNotEmpty(pc.getName())).findAny().orElse(null);
        post.getPostContents().forEach(pc ->{
            if (pc.isAutoTranslate() && srcTrans != null && isEmpty(pc.getName())){
                String srcLang = srcTrans.getLang();
                String targetLang = pc.getLang();
                if (isNotEmpty(srcTrans.getName())){
                    pc.setName(translateService.translate(srcTrans.getName(), srcLang, targetLang));
                    pc.setSlug(RegexUtils.normalize(pc.getName()));
                }

                if (isNotEmpty(srcTrans.getDescription())){
                    pc.setDescription(translateService.translate(srcTrans.getDescription(), srcLang, targetLang));
                }

                if (isNotEmpty(srcTrans.getContent())){
                    pc.setContent(translateService.translate(srcTrans.getContent(), srcLang, targetLang));
                }
            }
            if (pc.getId() == null){
                pc.setPost(post);
            }
        });
        return super.create(post);
    }

//    @Cacheable(value = POST_PAGING, key = "#slug") // Can't cache on entity or after mapping
    public Post getPostBySLug(String slug) {
        return getRepository().findBydPostContents_slug(slug);
    }

//    @Cacheable(value = POST_DETAIL, key = "#page") // Can't cache on entity or after mapping
    public Page<Post> getPublicPaging(int page) {
        Pageable pageReq = PageRequest.of(page, 6, Sort.by(Sort.Direction.DESC, "id")); // 6/12 is most adapt for grid 2,3,4 layout
        return getRepository().findAllByStatusEqualsAndTypeEquals(Status.ACTIVE, PostType.POST, pageReq);
    }
}
