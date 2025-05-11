package site.code4fun.controller.admin;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.ShiftEntity;
import site.code4fun.model.dto.ShiftDTO;
import site.code4fun.model.mapper.ShiftMapper;
import site.code4fun.service.ShiftService;

@Getter
@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.SHIFTS_ENDPOINT)
@RequiredArgsConstructor
@Lazy
public class AdminShiftController extends AdminAbstractBaseController<ShiftEntity, ShiftDTO, Long>{
    private final ShiftService service;
    private final ShiftMapper mapper;

}
