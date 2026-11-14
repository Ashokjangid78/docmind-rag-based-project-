package com.logic.service;

import com.logic.entity.DocumentMetadata;
import com.logic.repository.DocumentMetadataRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final VectorStore vectorStore;
    private final DocumentMetadataRepo documentMetadataRepo;
    private final AppProperties appProperties;

    public int ingest(DocumentMetadata documentMetadata, List<Document> parseDocs) {
        return 0;
    }
}
