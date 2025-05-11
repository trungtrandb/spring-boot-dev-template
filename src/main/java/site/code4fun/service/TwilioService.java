package site.code4fun.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.ApplicationProperties;

import javax.annotation.PostConstruct;

@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
public class TwilioService {

    private final ApplicationProperties properties;

    @PostConstruct
    void postConstruct(){
        Twilio.init(properties.getUserName(), properties.getPassword());
    }

    public String send(String receivePhoneNumber, String messageContent){
        try {
            Message message = Message.creator(
                            new PhoneNumber(receivePhoneNumber),
                            new PhoneNumber(properties.getTwilioPhoneNumber()),
                            messageContent).create();
            log.info("Twilio sending message to {}, sid {}", receivePhoneNumber, message.getSid());
            return message.getSid();
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return "";
    }
}
