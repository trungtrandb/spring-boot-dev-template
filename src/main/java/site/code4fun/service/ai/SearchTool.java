package site.code4fun.service.ai;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchResults;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class SearchTool {
    private final WebSearchEngine googleSearchEngine;
    private final WebSearchEngine tavilySearchEngine;

    @Tool("Searches Google for relevant data, given the query")
    private WebSearchResults searchGoogle(String query) {
        log.info("Searching for {}", query);
        return googleSearchEngine.search(query);
    }

    @Tool("Use tavily to Searches for relevant data, given the query")
    private WebSearchResults searchGeneral(String query) {
        log.info("Searching tavily for {}", query);
        return tavilySearchEngine.search(query);
    }
}