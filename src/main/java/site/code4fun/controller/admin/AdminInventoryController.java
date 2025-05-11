package site.code4fun.controller.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.InventoryEntity;
import site.code4fun.model.dto.InventoryDTO;
import site.code4fun.model.mapper.InventoryMapper;
import site.code4fun.service.InventoryService;

@Getter
@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.INVENTORIES_ENDPOINT)
@RequiredArgsConstructor
@Lazy
public class AdminInventoryController extends AdminAbstractBaseController<InventoryEntity, InventoryDTO, String>{
	private final InventoryService service;
	private final InventoryMapper mapper;

}
