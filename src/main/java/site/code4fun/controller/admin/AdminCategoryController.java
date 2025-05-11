package site.code4fun.controller.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.CategoryEntity;
import site.code4fun.model.dto.CategoryDTO;
import site.code4fun.model.mapper.CategoryMapper;
import site.code4fun.service.CategoryService;

@Getter
@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.CATEGORIES_ENDPOINT)
@RequiredArgsConstructor
@Lazy
public class AdminCategoryController extends AdminAbstractBaseController<CategoryEntity, CategoryDTO, Long>{

	private final CategoryService service;
	private final CategoryMapper mapper;
}
