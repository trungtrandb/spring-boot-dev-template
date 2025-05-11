package site.code4fun.service.ai;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistentChatMemoryStore implements ChatMemoryStore { // TODO consider save to database
    Map<Object, String> mapMess = new HashMap<>();

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return ChatMessageDeserializer.messagesFromJson(mapMess.get(memoryId));
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        mapMess.put(memoryId, ChatMessageSerializer.messagesToJson(messages));
    }

    @Override
    public void deleteMessages(Object memoryId) {
        mapMess.remove(memoryId);
    }
}
