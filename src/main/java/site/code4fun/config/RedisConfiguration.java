package site.code4fun.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import site.code4fun.service.queue.redis.DataChannelSignalSubscriber;
import site.code4fun.service.queue.redis.MessageSignalSubscriber;
import site.code4fun.service.queue.redis.MessageSubscriber;

import static site.code4fun.constant.QueueName.*;


@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configure serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, Long> rateLimitTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configure serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Long.class));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericToStringSerializer<>(Long.class));
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
                                                        MessageSignalSubscriber signalSubscriber,
                                                        DataChannelSignalSubscriber signalDataSubscriber,
                                                        MessageSubscriber messageSubscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // Add message listeners with error handling
        try {
            container.addMessageListener(new MessageListenerAdapter(messageSubscriber), 
                new ChannelTopic(CHAT_CHANNEL));
            container.addMessageListener(new MessageListenerAdapter(signalSubscriber), 
                new ChannelTopic(SIGNAL_CHANNEL));
            container.addMessageListener(new MessageListenerAdapter(signalDataSubscriber), 
                new ChannelTopic(SIGNAL_DATA_CHANNEL));
        } catch (Exception e) {
            log.error("Failed to initialize Redis message listeners", e);
            throw new RuntimeException("Failed to initialize Redis message listeners", e);
        }
        
        return container;
    }
}