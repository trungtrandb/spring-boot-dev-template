package site.code4fun.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import site.code4fun.model.ChatRoomEntity;
import site.code4fun.model.MessageEntity;
import site.code4fun.model.User;
import site.code4fun.model.dto.MessageDTO;
import site.code4fun.model.mapper.MessageMapper;
import site.code4fun.model.mapper.UserMapper;
import site.code4fun.model.request.SignalRequest;
import site.code4fun.repository.jpa.ChatRoomRepository;
import site.code4fun.repository.jpa.MessageRepository;
import site.code4fun.repository.jpa.UserRepository;
import site.code4fun.service.ai.Gemini;
import site.code4fun.service.queue.QueueService;
import site.code4fun.util.SecurityUtils;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;


@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
public class ChatService extends AbstractBaseService<ChatRoomEntity, Long> {

    @Getter(AccessLevel.PROTECTED)
    private final ChatRoomRepository repository;
    private final MessageRepository messageRepository;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    @Qualifier("redisImpl") private final QueueService queueService;
    private final Gemini gemini;
    private final FinancialAnalysisOrchestrator financialAnalysisOrchestrator;

    @Override
    public Page<ChatRoomEntity> getPaging(Map<String, String> mapRequestParam) {
        Page<ChatRoomEntity> rooms = super.getPaging(mapRequestParam);
        rooms.getContent().forEach(room -> room.setLastMessage(messageRepository.findFirstByRoom_idOrderByCreatedDesc(room.getId())));
        return rooms;
    }

    public Page<MessageEntity> getAllMessageByRoomId(Long id, Map<String, String> mapRequests) {
        log.info("mapRequests {}", mapRequests.toString());
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "created"));
        return messageRepository.findByRoomId(id, pageRequest);
    }

    @Override
    public ChatRoomEntity create(ChatRoomEntity req){
        Long currentUser = SecurityUtils.getUserId();
        if (currentUser != null){
            Set<User> user = req.getUsers();
            user.forEach(u -> req.addUser(userRepository.getReferenceById(u.getId())));
            req.addUser(userRepository.getReferenceById(currentUser));
            if (req.isChatBot()){
                req.setName("AI Assistant");
            }

            if (req.isFinanceBot()){
                req.setName("Finance Assistant");
            }
            if (isBlank(req.getName())){
                req.setName("New Room");
            }
            return getRepository().save(req);
        }
        return null;
    }

    public List<ChatRoomEntity> findAllByUserId(long userId){
        return getRepository().findByUsers_id(userId);
    }

    public void handleChatMessage(MessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
        if (auth != null){
            var userPrincipal =  (User) auth.getPrincipal();
            message.setCreatedBy(userMapper.entityToLite(userPrincipal));
        }
        message.setCreated(new Date());
        MessageEntity messageEntity = messageMapper.dtoToEntity(message);
        messageRepository.save(messageEntity);

        queueService.sendChatMessage(message);


        ChatRoomEntity room = getById(message.getRoom().getId());// Chatbot room
        if (room != null) {
            User bot = userRepository.findByEmailContainsIgnoreCase("bot");
            String aiMessage;
            if (room.isChatBot()){
                aiMessage = gemini.sendMessage(message.getRoom().getId().intValue(), message.getContent());
                sendBotMessage(aiMessage, room, bot);
            }else if (room.isFinanceBot()){
                financialAnalysisOrchestrator.analyzeStock(message.getContent(), room, bot);
            }

        }
    }

    public void handleSignal(Long roomId, SignalRequest message, SimpMessageHeaderAccessor headerAccessor) {
        log.info("handleSignal {}, accessorId {}", message, headerAccessor.getSessionId());
        if (isEmpty(message.getSessionId())){
            message.setSessionId(headerAccessor.getSessionId());
        }
        message.setRoomId(roomId);
        queueService.sendWebRtcSignal(message);
    }

    public void handleSignalDataChannel(Long roomId, SignalRequest message, SimpMessageHeaderAccessor headerAccessor) {
        log.info("handleSignalDataChannel {}, accessorId {}", message, headerAccessor.getSessionId());
        if (isEmpty(message.getSessionId())){
            message.setSessionId(headerAccessor.getSessionId());
        }
        message.setRoomId(roomId);
        queueService.sendWebRtcDataChannelSignal(message);
    }

    public boolean isMemberOfRoom(Long userId, Long roomId){
        ChatRoomEntity room = getById(roomId);
        if (room != null && room.isPrivate()){
            return room.getUsers().stream().anyMatch(user -> Objects.equals(userId, user.getId()));
        }
        return true;
    }

    public ChatRoomEntity findOneByCurrentUserAndOneUser(long userId){
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId !=  null){
            List<Long> roomIds = findAllByUserId(currentUserId).stream().map(ChatRoomEntity::getId).toList();
            List<ChatRoomEntity> rooms = getRepository().findByUsers_idAndIdIn(userId, roomIds);
            return rooms.stream().filter(room -> room.getUsers().size() == 2).findFirst().orElseGet(() ->{
                ChatRoomEntity newRoom = new ChatRoomEntity();
                Optional<User> user = userRepository.findById(userId);
                if (user.isPresent()){
                    newRoom.addUser(user.get());
                    newRoom.setName(user.get().getLastName());
                    return create(newRoom);
                }
                return null;
            });
        }
        return null;
    }

    private void sendBotMessage(String aiMessage, ChatRoomEntity room, User bot) {
        MessageEntity aiResponse = new MessageEntity();
        aiResponse.setCreated(new Date());
        aiResponse.setCreatedBy(bot);
        aiResponse.setRoom(room);
        aiResponse.setContent(aiMessage);
        messageRepository.save(aiResponse);
        queueService.sendChatMessage(messageMapper.entityToDto(aiResponse));
    }
}