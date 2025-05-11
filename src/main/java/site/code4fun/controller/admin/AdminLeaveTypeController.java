package site.code4fun.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.EventEntity;
import site.code4fun.model.LeaveTypeEntity;
import site.code4fun.model.dto.LeaveTypeDTO;
import site.code4fun.model.mapper.LeaveTypeMapper;
import site.code4fun.model.request.DeleteRequest;
import site.code4fun.service.LeaveTypeService;

import java.util.Map;

@Getter
@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.LEAVE_TYPE_ENDPOINT)
@RequiredArgsConstructor
@Lazy
public class AdminLeaveTypeController extends AdminAbstractBaseController<LeaveTypeEntity, LeaveTypeDTO, Long>{
	private final LeaveTypeService service;
	private final LeaveTypeMapper mapper;

	@GetMapping("/holidays")
	@Operation( summary = "Get all paging", description = "Get all data with paging")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful") })
	public Page<EventEntity> getHolidays(@RequestParam Map<String, String> searchRequest) {
		return getService().getAllHoliday(searchRequest);
	}

	@PostMapping("/holidays")
	@Transactional
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successful")
	})
	public EventEntity save(@RequestBody EventEntity request){
		return getService().createHoliday(request);
	}

	@PostMapping("/holidays/delete")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	@Operation( summary = "Delete list", description = "Delete records by object list object Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Successful")
	})
	public void deleteHolidayById(@RequestBody DeleteRequest<Long> request){
		getService().deleteHolidayByIds(request.getId());
	}
}
