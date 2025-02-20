package io.fairspace.saturn.services.search;

import io.fairspace.saturn.services.BaseApp;

import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;
import static spark.Spark.post;

public class SearchApp extends BaseApp {
    private final SearchService searchService;
    private final FileSearchService fileSearchService;

    public SearchApp(String basePath, SearchService searchService, FileSearchService fileSearchService) {
        super(basePath);
        this.searchService = searchService;
        this.fileSearchService = fileSearchService;
    }

    @Override
    protected void initApp() {
        post("/files", (req, res) -> {
            res.type(APPLICATION_JSON.asString());
            var request = mapper.readValue(req.body(), FileSearchRequest.class);
            var searchResult = fileSearchService.searchFiles(request);

            SearchResultsDTO resultDto = SearchResultsDTO.builder()
                    .results(searchResult)
                    .query(request.getQuery())
                    .build();

            return mapper.writeValueAsString(resultDto);
        });

        post("/lookup", (req, res) -> {
            res.type(APPLICATION_JSON.asString());
            var request = mapper.readValue(req.body(), LookupSearchRequest.class);
            var results = searchService.getLookupSearchResults(request);
            return mapper.writeValueAsString(results);
        });
    }
}
