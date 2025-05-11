package site.code4fun.service.queue.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import site.code4fun.model.MessageEntity;
import site.code4fun.model.dto.MessageDTO;
import site.code4fun.model.mapper.MessageMapper;
import site.code4fun.repository.jpa.MessageRepository;

import static site.code4fun.constant.QueueName.CHAT_ROOM_TOPIC;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    @Override
    @SneakyThrows
    public void onMessage(@NotNull Message message, byte[] pattern) {
        MessageDTO messageDTO = objectMapper.readValue(message.getBody(), MessageDTO.class);
        if (messageDTO.getCreated() == null && messageDTO.getCreatedBy().getId() == 4) {
            MessageEntity messageEntity = messageMapper.dtoToEntity(messageDTO);
            messageRepository.save(messageEntity);
        }

        messagingTemplate.convertAndSend(CHAT_ROOM_TOPIC + messageDTO.getRoom().getId(), messageDTO);
    }
}
