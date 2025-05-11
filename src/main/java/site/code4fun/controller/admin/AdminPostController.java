package site.code4fun.controller.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.Post;
import site.code4fun.model.dto.PostDTO;
import site.code4fun.model.mapper.PostMapper;
import site.code4fun.service.PostService;

@Getter
@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.POSTS_ENDPOINT)
@RequiredArgsConstructor
@Lazy
public class AdminPostController extends AdminAbstractBaseController<Post, PostDTO, Long> {
	private final PostService service;
	private final PostMapper mapper;
}
