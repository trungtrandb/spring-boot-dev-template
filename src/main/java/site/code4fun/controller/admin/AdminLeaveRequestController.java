package site.code4fun.controller.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.LeaveRequestEntity;
import site.code4fun.model.dto.LeaveRequestDTO;
import site.code4fun.model.mapper.LeaveRequestMapper;
import site.code4fun.service.LeaveRequestService;

@Getter
@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.LEAVE_REQUESTS_ENDPOINT)
@RequiredArgsConstructor
@Lazy
public class AdminLeaveRequestController extends AdminAbstractBaseController<LeaveRequestEntity, LeaveRequestDTO, Long>{

	private final LeaveRequestService service;
	private final LeaveRequestMapper mapper;

}
