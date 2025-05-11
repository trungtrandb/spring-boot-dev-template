package site.code4fun.service.queue.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.NotSupportedException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import site.code4fun.model.dto.MessageDTO;
import site.code4fun.model.dto.SendMailDTO;
import site.code4fun.model.request.SignalRequest;
import site.code4fun.service.queue.QueueService;

import static site.code4fun.constant.QueueName.*;


@Component
@Slf4j
@RequiredArgsConstructor
public class RedisImpl implements QueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;


    @Override
    @SneakyThrows
    public void sendChatMessage(MessageDTO message){
        redisTemplate.convertAndSend(CHAT_CHANNEL, objectMapper.writeValueAsString(message));
    }

    @Override
    @SneakyThrows
    public void sendSmsOrMailContact(String queueName, SendMailDTO dto) {
        throw new NotSupportedException("Not supported");
    }


    @Override
    @SneakyThrows
    public void sendWebRtcSignal(SignalRequest message) {
        redisTemplate.convertAndSend(SIGNAL_CHANNEL, objectMapper.writeValueAsString(message));
    }

    @Override
    @SneakyThrows
    public void sendWebRtcDataChannelSignal(SignalRequest message) {
        redisTemplate.convertAndSend(SIGNAL_DATA_CHANNEL, objectMapper.writeValueAsString(message));
    }

}
