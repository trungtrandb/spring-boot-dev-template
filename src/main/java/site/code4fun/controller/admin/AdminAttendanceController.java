package site.code4fun.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.AttendanceEntity;
import site.code4fun.model.dto.AttendanceDTO;
import site.code4fun.model.mapper.AttendanceMapper;
import site.code4fun.service.AttendanceService;

@Getter
@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.ATTENDANCES_ENDPOINT)
@RequiredArgsConstructor
@Lazy
public class AdminAttendanceController extends AdminAbstractBaseController<AttendanceEntity, AttendanceDTO, Long>{

	private final AttendanceService service;
	private final AttendanceMapper mapper;

	@PostMapping
	@Transactional
	@ResponseStatus(HttpStatus.CREATED)
	@Operation( summary = "Create new", description = "Create new object with object id 'must' be null")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successful")
	})
	@Override
	public AttendanceDTO create(@RequestBody(required = false) AttendanceDTO request){
		AttendanceEntity u = getService().create(getMapper().dtoToEntity(request));
		return getMapper().entityToDto(u);
	}

	@GetMapping("/today")
	public AttendanceDTO getTodayAttendance(){
		return getMapper().entityToDto(getService().getTodayAttendance());
	}

	@GetMapping("/my-statistic")
	public Object getStatistic(){
		return getService().getStatistic();
	}


}
