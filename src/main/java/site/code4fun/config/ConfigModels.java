package site.code4fun.config;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.google.customsearch.GoogleCustomWebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import site.code4fun.service.ai.ModelName;

import static dev.langchain4j.model.anthropic.AnthropicChatModelName.*;
import static dev.langchain4j.model.openai.OpenAiImageModelName.DALL_E_3;
import static site.code4fun.service.ai.ModelName.GPT_4_MINI;

@Configuration
public class ConfigModels {

    @Bean
    @Lazy
    public ChatLanguageModel geminiModel(@Value("${langchain4j.google-ai-gemini.api-key}") String apiKey) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .responseFormat(ResponseFormat.TEXT)
                .modelName(ModelName.GEMINI_2_FLASH)
                .build();
    }

    @Bean
    @Lazy
    public ChatLanguageModel openAiModel(@Value("${langchain4j.open-ai.chat-model.api-key}") String apiKey) {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(GPT_4_MINI)
                .build();
    }

    @Bean
    @Lazy
    public ChatLanguageModel openAiDemo() {
        return OpenAiChatModel.builder()
                .apiKey("demo")
                .modelName(GPT_4_MINI)
                .build();
    }

    @Bean
    @Lazy
    public ChatLanguageModel anthropicModel(@Value("${langchain4j.anthropic.api-key}") String apiKey){ // Need pay
        return  AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName(CLAUDE_3_HAIKU_20240307)
                .build();
    }

    @Bean
    @Lazy
    public WebSearchEngine tavilySearchEngine(@Value("${langchain4j.search-engine-tavily.api-key}") String apiKey){
        return TavilyWebSearchEngine.builder()
                .apiKey(apiKey)
                .build();
    }

    @Bean
    @Lazy
    public WebSearchEngine googleSearchEngine(@Value("${langchain4j.engine-google-custom.api-key}") String apiKey,
                                              @Value("${langchain4j.engine-google-custom.csi}") String csi) {
        return GoogleCustomWebSearchEngine.builder()
                .apiKey(apiKey)
                .csi(csi)
                .build();
    }

    @Bean
    @Lazy
    public ImageModel imageModel(@Value("${langchain4j.open-ai.chat-model.api-key}") String apiKey) { // Need pay
        return OpenAiImageModel.builder()
                .apiKey(apiKey)
                .modelName(DALL_E_3)
                .build();
    }
}
