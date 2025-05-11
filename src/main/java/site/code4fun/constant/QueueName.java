package site.code4fun.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueueName {

    // Kafka
    public static final String KAFKA_TOPIC_NAME_SEND_MAIL = "queue_send_mail";
    public static final String KAFKA_TOPIC_NAME_SEND_SMS = "queue_send_sms";
    public static final String KAFKA_GROUP_ID = "group1";

    // Redis
    public static final String CHAT_CHANNEL = "chatChanel";
    public static final String SIGNAL_CHANNEL = "signalChanel";
    public static final String SIGNAL_DATA_CHANNEL = "signalDataChanel";

    public static final String WEBRTC_SIGNAL_TOPIC = "/topic/signal/";
    public static final String WEBRTC_SIGNAL_DATA_TOPIC = "/topic/signal-data/";
    public static final String CHAT_ROOM_TOPIC = "/topic/messages/";
}
