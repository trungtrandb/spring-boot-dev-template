package site.code4fun.controller.admin;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.ContactEntity;
import site.code4fun.model.dto.ContactDTO;
import site.code4fun.model.mapper.ContactMapper;
import site.code4fun.model.request.SendMailRequest;
import site.code4fun.service.ContactService;

import java.util.List;

@Getter
@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.CONTACTS_ENDPOINT)
@RequiredArgsConstructor
@Lazy
public class AdminContactController extends AdminAbstractBaseController<ContactEntity, ContactDTO, Long>{
    private final ContactService service;
    private final ContactMapper mapper;

    @GetMapping(value="/{ids}")
    public List<ContactDTO> getByIds(@PathVariable List<Long> ids) {
        return service.getByIds(ids).stream().map(mapper::entityToDto).toList();
    }

    @PostMapping("/bulk/send")
    @Transactional
    public void bulkSend(@RequestBody SendMailRequest request){
        service.bulkSend(request);
    }
    @GetMapping("/sync")
    @Transactional
    public List<ContactDTO> sync(){
        return service.sync().stream().map(mapper::entityToDto).toList();
    }

}
