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
import site.code4fun.model.request.SignalRequest;

import static site.code4fun.constant.QueueName.WEBRTC_SIGNAL_TOPIC;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageSignalSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @SneakyThrows
    public void onMessage(@NotNull Message message, byte[] pattern) {
        SignalRequest myMessage = objectMapper.readValue(message.getBody(), SignalRequest.class);
        messagingTemplate.convertAndSend(WEBRTC_SIGNAL_TOPIC + myMessage.getRoomId(), myMessage);
    }
}
