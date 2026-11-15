package com.logic.service;

import com.logic.config.AppProperties;
import com.logic.entity.DocumentMetadata;
import com.logic.entity.Enum.DocumentStatus;
import com.logic.exception.DocumentProcessingException;
import com.logic.repository.DocumentMetadataRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final VectorStore vectorStore;
    private final DocumentMetadataRepo documentMetadataRepo;
    private final AppProperties appProperties;

    public int ingest(DocumentMetadata metadata, List<Document> parseDocs) {
        log.info("Ingesting document [id = {},name = {},pages={}]",metadata.getId(),metadata.getFilename(),metadata.getFileSize());

        try{
            metadata.setStatus(DocumentStatus.PROCESSING);
            metadata.setTotalPages(parseDocs.size());

            // 1. text chunking using TokenTextSpliter

            TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
                    .withChunkSize(appProperties.getRag().getChunkSize())
                    .withMinChunkSizeChars(appProperties.getRag().getMinChunkSizeChars())
                    .withMinChunkLengthToEmbed(appProperties.getRag().getMinChunkLengthToEmbed())
                    .withMaxNumChunks(appProperties.getRag().getMaxNumChunks())
                    .withKeepSeparator(true)
                    .build();

           List<Document>chunks= tokenTextSplitter.apply(parseDocs);
           if (chunks.isEmpty()){
               metadata.setStatus(DocumentStatus.FAILED);
               metadata.setErrorMessage("Document apperars to be empty or unscannable");
               documentMetadataRepo.save(metadata);
           }


           //  2. Meta data enrichment on each chunk
            List<Document>enrichedChunks = new ArrayList<>();
           for (int i = 0 ; i < chunks.size(); i++){
               Document chunk = chunks.get(i);
               Map<String,Object>enrichedMetadata = new HashedMap<>(chunk.getMetadata());
               enrichedMetadata.put("documentId", metadata.getId().toString());
               enrichedMetadata.put("fileName", metadata.getFilename());
               enrichedMetadata.put("contentType", metadata.getContentType());
               enrichedMetadata.put("chunkIndex",i);
               // preserve to calculate page number if avilable
               Object pageNumber = chunk.getMetadata().get("page_number");
               if (pageNumber == null ){
                   pageNumber = chunk.getMetadata().get("pageNumber");
               }

               if (pageNumber != null){
                   enrichedMetadata.put("pageNumber", pageNumber);

               }
               Document enrichedDoc = new Document(chunk.getText(),enrichedMetadata);
               enrichedChunks.add(enrichedDoc);

           }

//           3. write chunks and embedding to Qdrant vector
            log.info("writing {} vector chunks to Qdrant vector for document: {} ", enrichedChunks.size(),metadata.getFilename());
           vectorStore.add(enrichedChunks);

//           4. Update document status to index
            metadata.setStatus(DocumentStatus.INDEXED);
            metadata.setTotalChunks(enrichedChunks.size());
            metadata.setErrorMessage(null);
            documentMetadataRepo.save(metadata);
            log.info("Successfully indexed document [id = {}, chunks = {}",metadata.getId(),metadata.getFilename(),enrichedChunks.size());

            return enrichedChunks.size();

        }
        catch (Exception ex){
            log.error("Failed to ingest document into vetor store: {}",metadata.getFilename(),ex);
            metadata.setStatus(DocumentStatus.FAILED);
            metadata.setErrorMessage(ex.getMessage());
            documentMetadataRepo.save(metadata);
            throw new DocumentProcessingException("Failed to index document:" + ex.getMessage(),ex);

        }

    }
}
