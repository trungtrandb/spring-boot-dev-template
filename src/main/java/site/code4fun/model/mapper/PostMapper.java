package site.code4fun.model.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.Post;
import site.code4fun.model.PostContent;
import site.code4fun.model.dto.PostContentDTO;
import site.code4fun.model.dto.PostDTO;
import site.code4fun.model.dto.PostLiteDTO;
import site.code4fun.model.dto.PostTmp;
import site.code4fun.util.SecurityUtils;
import site.code4fun.util.UrlParserUtils;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface PostMapper extends BaseMapper<Post, PostDTO>{
    PostContentDTO postContentToPostContentDTO(PostContent post);
    PostContent postContentDtoToPostContent(PostContentDTO post);

    default PostTmp entityToTmp(Post entity){
        PostContent pc = entity.getPostContents().stream()
                .filter(content -> SecurityUtils.getCurrentLang().equalsIgnoreCase(content.getLang())).findFirst()
                .orElse(new PostContent());
        PostTmp tmp = new PostTmp();
        tmp.setId(entity.getId());
        tmp.setSlug(UrlParserUtils.buildPrettyURL(pc.getName()));
        tmp.setLanguage(pc.getLang());
        tmp.setFaq_type("global");
        tmp.setFaq_title(pc.getName());
        tmp.setFaq_description(pc.getContent());
        tmp.setTranslated_languages(Collections.singletonList(pc.getLang()));
        return tmp;
    }

    default Map<String, PostContentDTO> map(Set<PostContent> value){
        Map<String, PostContentDTO> mapContent = new HashMap<>();
        value.forEach(postContent -> mapContent.put(postContent.getLang(), postContentToPostContentDTO(postContent)));
        return mapContent;
    }

    default Set<PostContent> map(Map<String, PostContentDTO> value) {
        return Optional.ofNullable(value)
                .map(val -> val.entrySet().stream()
                        .map(entry -> {
                            PostContent pc = postContentDtoToPostContent(entry.getValue());
                            pc.setLang(entry.getKey());
                            return pc;
                        })
                        .collect(Collectors.toSet()))
                .orElseGet(HashSet::new);
    }

    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "lang", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "content", ignore = true)
    PostLiteDTO postToPostLiteDTO(Post post);

    @AfterMapping
    default void completePostLiteDTO(Post post, @MappingTarget PostLiteDTO dto) {
        if (post == null || post.getPostContents() == null || post.getPostContents().isEmpty()) {
            return;
        }

        String currentLang = SecurityUtils.getCurrentLang();

        PostContent pc = post.getPostContents().stream()
            .filter(postContent -> postContent != null &&
                postContent.getLang() != null &&
                currentLang.equalsIgnoreCase(postContent.getLang()))
            .findFirst()
            .orElseGet(() -> post.getPostContents().stream()
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null));

        if (pc != null) {
            dto.setSlug(pc.getSlug());
            dto.setLang(pc.getLang());
            dto.setName(pc.getName());
            dto.setDescription(pc.getDescription());
            dto.setContent(pc.getContent());
        }
    }
}