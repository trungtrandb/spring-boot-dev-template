package site.code4fun.controller.admin;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.PromptEntity;
import site.code4fun.model.mapper.PromptMapper;
import site.code4fun.service.PromptService;

@Getter
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + "/prompts")
@RequiredArgsConstructor
@Lazy
@RestController
public class AdminPromptController extends AdminAbstractBaseController<PromptEntity, PromptEntity, Long>{
    private final PromptService service;
    private final PromptMapper mapper;
}
