package site.code4fun.controller.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.TagEntity;
import site.code4fun.model.dto.TagDTO;
import site.code4fun.model.mapper.TagMapper;
import site.code4fun.service.TagService;

@Getter
@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.TAGS_ENDPOINT)
@RequiredArgsConstructor
@Lazy
public class AdminTagController extends AdminAbstractBaseController<TagEntity, TagDTO, Long>{
	private final TagService service;
	private final TagMapper mapper;

}
