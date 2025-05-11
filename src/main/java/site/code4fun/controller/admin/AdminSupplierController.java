package site.code4fun.controller.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.SupplierEntity;
import site.code4fun.model.dto.SupplierDTO;
import site.code4fun.model.mapper.SupplierMapper;
import site.code4fun.service.SupplierService;

@Getter
@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.SUPPLIER_ENDPOINT)
@RequiredArgsConstructor
@Lazy
public class AdminSupplierController extends AdminAbstractBaseController<SupplierEntity, SupplierDTO, Long>{
	private final SupplierService service;
	private final SupplierMapper mapper;

}
