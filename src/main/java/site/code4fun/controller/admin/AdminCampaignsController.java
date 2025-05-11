package site.code4fun.controller.admin;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.CampaignEntity;
import site.code4fun.model.dto.CampaignDTO;
import site.code4fun.model.mapper.CampaignMapper;
import site.code4fun.service.CampaignService;

import java.util.List;

@Getter
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.CAMPAIGNS_ENDPOINT)
@RequiredArgsConstructor
@Lazy
@RestController
public class AdminCampaignsController extends AdminAbstractBaseController<CampaignEntity, CampaignDTO, Long>{
    private final CampaignService service;
    private final CampaignMapper mapper;

    @GetMapping("/sync")
    @Transactional
    public List<CampaignDTO> sync(){
        return service.sync().stream().map(mapper::entityToDto).toList();
    }
}
