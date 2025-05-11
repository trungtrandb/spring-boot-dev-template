package site.code4fun.service.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.code4fun.model.dto.MessageDTO;
import site.code4fun.model.dto.SendMailDTO;
import site.code4fun.model.request.SignalRequest;

import static site.code4fun.constant.AppConstants.NOT_IMPLEMENT;

@Component
@Slf4j
@RequiredArgsConstructor
public class ActiveMqImpl implements QueueService{
    @Override
    public void sendSmsOrMailContact(String queueName, SendMailDTO dto) {
        throw new UnsupportedOperationException(NOT_IMPLEMENT);
    }

    @Override
    public void sendChatMessage(MessageDTO message) {
        throw new UnsupportedOperationException(NOT_IMPLEMENT);
    }

    @Override
    public void sendWebRtcSignal(SignalRequest message) {
        throw new UnsupportedOperationException(NOT_IMPLEMENT);
    }
}
