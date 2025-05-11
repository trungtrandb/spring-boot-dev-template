//package site.code4fun.service.queue;
//
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.annotation.RetryableTopic;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.retry.annotation.Backoff;
//import org.springframework.stereotype.Component;
//import site.code4fun.constant.QueueName;
//import site.code4fun.model.dto.SendMailDTO;
//import site.code4fun.service.TwilioService;
//import site.code4fun.service.email.EmailService;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class KafKaQueueImpl implements QueueService {
//    private final EmailService emailService;
//    private final TwilioService twilioService;
//    private final KafkaTemplate<String, SendMailDTO> kafkaTemplate;
//    private static final String LOG_FORMAT = " [x] Received {}, topic {}, group {}";
//
//    // Producer
//    @Override
//    public void sendSmsOrMailContact(String queueName, SendMailDTO dto) {
//        kafkaTemplate.send(queueName, dto);
//    }
//
//    // Listener
//    @SneakyThrows
//    @RetryableTopic(attempts = "5", backoff = @Backoff(delay = 2000L, multiplier = 2))
//    @KafkaListener(topics = QueueName.KAFKA_TOPIC_NAME_SEND_MAIL, id = QueueName.KAFKA_GROUP_ID, autoStartup = "false")
//    private void mailConsumer(SendMailDTO sendMailMessage) {
//        log.info(LOG_FORMAT, sendMailMessage, QueueName.KAFKA_TOPIC_NAME_SEND_MAIL, QueueName.KAFKA_GROUP_ID);
//        emailService.sendEmail(sendMailMessage.getEmailAddress(), sendMailMessage.getSubject(), sendMailMessage.getContent(), false, true);
//    }
//
//    @KafkaListener(topics = QueueName.KAFKA_TOPIC_NAME_SEND_MAIL + "-dlt", id = "dltGroup")
//    private void deadLetterTopic(SendMailDTO sendMailMessage) {
//        log.info(" [x] Received {}, topic {}, group dltGroup", sendMailMessage, QueueName.KAFKA_TOPIC_NAME_SEND_MAIL + "-dlt");
//    }
//
//    @KafkaListener(topics = QueueName.KAFKA_TOPIC_NAME_SEND_SMS, groupId = QueueName.KAFKA_GROUP_ID,  autoStartup = "false")
//    private void messageConsumer(SendMailDTO sendMailMessage) {
//        log.info(LOG_FORMAT, sendMailMessage, QueueName.KAFKA_TOPIC_NAME_SEND_SMS, QueueName.KAFKA_GROUP_ID);
//        twilioService.send(sendMailMessage.getPhone(), sendMailMessage.getContent());
//    }
//}
