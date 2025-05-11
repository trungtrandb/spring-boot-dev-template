package site.code4fun.service.queue;

import jakarta.transaction.NotSupportedException;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import site.code4fun.model.dto.MessageDTO;
import site.code4fun.model.dto.SendMailDTO;
import site.code4fun.model.request.SignalRequest;

@Component
public interface QueueService {
    void sendSmsOrMailContact(String queueName, SendMailDTO dto);

    @SneakyThrows
    default void sendChatMessage(MessageDTO message) {
        throw new NotSupportedException("Not supported, try  use redis instead"); //kafka need to assign unique group id for each replica, can't auto scale-out
    }

    @SneakyThrows
    default void sendWebRtcSignal(SignalRequest message) {
        throw new NotSupportedException("Not supported, try use redis instead"); //kafka need to assign unique group id for each replica, can't auto scale-out
    }

    @SneakyThrows
    default void sendWebRtcDataChannelSignal(SignalRequest message) {
        throw new NotSupportedException("Not supported, try use redis instead"); //kafka need to assign unique group id for each replica, can't auto scale-out
    }
}
