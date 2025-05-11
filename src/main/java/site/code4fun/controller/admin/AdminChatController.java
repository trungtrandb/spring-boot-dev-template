package site.code4fun.controller.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.model.ChatRoomEntity;
import site.code4fun.model.dto.ChatRoomDTO;
import site.code4fun.model.dto.MessageDTO;
import site.code4fun.model.mapper.ChatRoomMapper;
import site.code4fun.model.mapper.MessageMapper;
import site.code4fun.model.request.SignalRequest;
import site.code4fun.service.ChatService;

import java.util.Map;

@RestController
@RequestMapping("/admin/chat/rooms")
@Slf4j
@RequiredArgsConstructor
@Lazy
@Getter
public class AdminChatController extends AdminAbstractBaseController<ChatRoomEntity, ChatRoomDTO, Long>{

    private final ChatService service;
    private final ChatRoomMapper mapper;
    private final MessageMapper messageMapper;

    @GetMapping(value = "/{id}/messages")
    public Page<MessageDTO> getRoomMessages(@PathVariable Long id, @RequestParam Map<String, String> mapRequests) {
        return service.getAllMessageByRoomId(id, mapRequests).map(messageMapper::entityToDto);
    }

    @Transactional
    @GetMapping("/users/{id}")
    public ChatRoomDTO getOneByUserId(@PathVariable long id){
        return mapper.entityToDto(service.findOneByCurrentUserAndOneUser(id));
    }

    // Handle websocket topic
    @Transactional(propagation = Propagation.NEVER)
    @MessageMapping("/chat/{id}")  // maps to /app/chat/roomId
    public void sendMessage(@DestinationVariable("id")Long id, MessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
        log.info("Sending mess to room Id: {}, message: {}, user: {}", id, message, headerAccessor.getUser());
        service.handleChatMessage(message, headerAccessor);
    }

    // Don't need to query db, disable transaction for reduce latency
    @MessageMapping("/signal/{id}")  // maps to /app/signal/roomId
    @Transactional(propagation = Propagation.NEVER)
    public void signalVideoCall(@DestinationVariable("id")Long id, SignalRequest message, SimpMessageHeaderAccessor headerAccessor) {
        service.handleSignal(id, message, headerAccessor);
    }

    @MessageMapping("/signal/data/{id}")  // maps to /app/signal/data/roomId
    @Transactional(propagation = Propagation.NEVER)
    public void signalDataChannel(@DestinationVariable("id")Long id, SignalRequest message, SimpMessageHeaderAccessor headerAccessor) {
        service.handleSignalDataChannel(id, message, headerAccessor);
    }
}
