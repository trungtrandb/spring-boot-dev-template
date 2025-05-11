package site.code4fun.service.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.*;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.web.search.WebSearchEngine;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

@Service
@Lazy
@Slf4j
public class Gemini {
    private final Assistant assistant;

    private Gemini(SearchTool tools,
                   @Qualifier("googleSearchEngine") WebSearchEngine webSearchEngine,
                   @Qualifier("geminiModel") ChatLanguageModel chatLanguageModel) {

        assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(tools)
                .retrievalAugmentor(retrievalAugmentor(webSearchEngine))
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(10)
                        .chatMemoryStore(new PersistentChatMemoryStore())
                        .build())
                .build();
    }

    @SneakyThrows
    public String sendMessage(int id, String message) {
        return assistant.chat(id, message).content();
    }

    private RetrievalAugmentor retrievalAugmentor(WebSearchEngine webSearchEngine){
        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        EmbeddingStore<TextSegment> embeddingStore = embed(embeddingModel);
        ContentRetriever embeddingStoreContentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();

        ContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
                .webSearchEngine(webSearchEngine)
                .maxResults(3)
                .build();

        QueryRouter queryRouter = new DefaultQueryRouter(embeddingStoreContentRetriever, webSearchContentRetriever);

        return DefaultRetrievalAugmentor.builder()
                .queryRouter(queryRouter)
                .build();
    }

    @SneakyThrows
    private EmbeddingStore<TextSegment> embed(EmbeddingModel embeddingModel) {
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        try{
            URL fileUrl = this.getClass().getClassLoader().getResource("policy.txt");

            DocumentParser documentParser = new TextDocumentParser();
            assert fileUrl != null;
            Document document = loadDocument(Paths.get(fileUrl.toURI()), documentParser);

            DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
            List<TextSegment> segments = splitter.split(document);

            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

            embeddingStore.addAll(embeddings, segments);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return embeddingStore;
    }

    private interface Assistant {
        @SystemMessage("Your name are Celine," +
                "an expert consultant," +
                "answer my question simple most as you can, " +
                "dont explain before i need but you can ask me more questions to clarify about my purpose," +
                "you can use web search engine for more information")
        Result<String> chat(@MemoryId int memoryId, @UserMessage String userMessage);
    }

}



