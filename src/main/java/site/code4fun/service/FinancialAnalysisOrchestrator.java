package site.code4fun.service;

import com.google.gson.Gson;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.code4fun.constant.Language;
import site.code4fun.constant.Period;
import site.code4fun.model.ChatRoomEntity;
import site.code4fun.model.MessageEntity;
import site.code4fun.model.User;
import site.code4fun.model.mapper.MessageMapper;
import site.code4fun.repository.jpa.MessageRepository;
import site.code4fun.service.ai.*;
import site.code4fun.service.ai.dto.MatchPrice;
import site.code4fun.service.queue.QueueService;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class FinancialAnalysisOrchestrator {
    private final StockService stockService;

    // Các Agents
    private final TechnicalIndicatorAgent technicalAgent;
    private final WarrenBuffetAgent buffetAgent;
    private final BenjaminGrahamAgent grahamAgent;
    private final SummarizerAdvisorAgent summarizerAgent;
    private final Gson gson;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final @Qualifier("redisImpl")QueueService queueService;


    public FinancialAnalysisOrchestrator(StockService tools,
                                         ChatLanguageModel openAiDemo,
                                         ChatLanguageModel geminiModel,
                                         Gson gson,
                                         TechnicalIndicatorAgent technicalAgent,
                                         MessageRepository messageRepository,
                                         MessageMapper messageMapper,
                                         @Qualifier("redisImpl")QueueService queueService,
                                         @Value("${langchain4j.google-ai-gemini.api-key}") String apiKey) {
        this.stockService = tools;
        this.gson = gson;
        this.technicalAgent = technicalAgent;
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.queueService = queueService;

        this.buffetAgent = AiServices.builder(WarrenBuffetAgent.class)
                .chatLanguageModel(openAiDemo)
                .tools(tools)
                .build();

        this.grahamAgent = AiServices.builder(BenjaminGrahamAgent.class)
                .chatLanguageModel(geminiModel)
                .tools(tools)
                .build();

         this.summarizerAgent = AiServices.builder(SummarizerAdvisorAgent.class)
                .chatLanguageModel(
                        GoogleAiGeminiChatModel.builder()
                                .apiKey(apiKey)
                                .responseFormat(ResponseFormat.TEXT)
                                .modelName(ModelName.GEMINI_2_5_PRO_EXP)
                                .build()
                )
                .build();
    }

    public void analyzeStock(String ticker, ChatRoomEntity room, User bot) {
        List<?> reports = stockService.getReportByYear(ticker, Period.Y,2024, Language.EN);
        String reportAsString = gson.toJson(reports);
        MatchPrice latestPrice = stockService.getLatestPrice(ticker);
        String priceString = gson.toJson(latestPrice);
        try {
            String techResult = technicalAgent.analyze(ticker, LocalDate.now());
            System.out.println("Done technicalAgent: " + techResult);

            String buffetResult = buffetAgent.analyze(reportAsString, priceString);
            sendBotMessage(buffetResult, room, bot);

            String grahamResult = grahamAgent.analyze(reportAsString, priceString);
            sendBotMessage(grahamResult, room, bot);

            String finalAdvice = summarizerAgent.summarizeAndAdvise(techResult, buffetResult, grahamResult, gson.toJson(latestPrice));
            sendBotMessage(finalAdvice, room, bot);
        } catch (Exception e) {
            sendBotMessage(e.getMessage(), room, bot);
        }
    }

    private void sendBotMessage(String aiMessage, ChatRoomEntity room, User bot) {
        MessageEntity aiResponse = new MessageEntity();
        aiResponse.setCreated(new Date());
        aiResponse.setCreatedBy(bot);
        aiResponse.setRoom(room);
        aiResponse.setContent(aiMessage);
        messageRepository.save(aiResponse);
        queueService.sendChatMessage(messageMapper.entityToDto(aiResponse));
    }
}