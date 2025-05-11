package site.code4fun.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.MimeType;
import site.code4fun.model.mapper.BaseMapper;
import site.code4fun.model.request.DeleteRequest;
import site.code4fun.service.BaseService;

import java.util.List;
import java.util.Map;

import static site.code4fun.constant.AppConstants.HEADER_CONTENT_DISPOSITION;

@Transactional(readOnly = true)
public abstract class AdminAbstractBaseController<E,D,I> {
    protected abstract BaseService<E, I> getService();
    protected abstract BaseMapper<E, D> getMapper();

    @GetMapping
    @Operation( summary = "Get all paging", description = "Get all data with paging")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful") })
    public Page<D> getAllPaging(@RequestParam Map<String, String> searchRequest) {
        return getService().getPaging(searchRequest).map(e -> getMapper().entityToDto(e));
    }

    @GetMapping("/{id}")
    @Operation( summary = "Get one by Id", description = "Get one by object id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "400", description = "Object not found")
    })
    public D getById(@PathVariable I id){
        return getMapper().entityToDto(getService().getById(id));
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation( summary = "Delete list", description = "Delete records by object list object Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful")
    })
    public void deleteAllById(@RequestBody DeleteRequest<I> request){
        getService().deleteByIds(request.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation( summary = "Delete one by id", description = "Delete record by object Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful"),
            @ApiResponse(responseCode = "400", description = "Object not found")
    })
    public void deleteById(@PathVariable I id){
        getService().delete(id);
    }


    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    @Operation( summary = "Create new", description = "Create new object with object id 'must' be null")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful")
    })
    public D create(@RequestBody @Valid D request){
        E u = getService().create(getMapper().dtoToEntity(request));
        return getMapper().entityToDto(u);
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation( summary = "Replace by id", description = "Find object by id and replace all updatable fields with request object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "400", description = "Object not found")
    })
    public D replace(@PathVariable I id, @RequestBody D request){
        E u = getService().update(id, getMapper().dtoToEntity(request));
        return getMapper().entityToDto(u);
    }

    @GetMapping("/export")
    @Operation( summary = "Export", description = "Export data as xlsx file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
    })
    public ResponseEntity<ByteArrayResource> export(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MimeType.APPLICATION_XLSX);
        headers.setAccessControlExposeHeaders(List.of(HEADER_CONTENT_DISPOSITION));
        headers.add(HEADER_CONTENT_DISPOSITION, "attachment; filename=export.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .body(getService().export());
    }
}
