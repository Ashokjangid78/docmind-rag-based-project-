package com.logic.service;

import com.logic.dto.DocumentResponseDto;
import com.logic.entity.DocumentMetadata;
import com.logic.entity.Enum.DocumentStatus;
import com.logic.repository.DocumentMetadataRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentMetadataService {

    private final DocumentMetadataRepo documentMetadataRepo;
    private final DocumentParserService documentParserService;
    private final DocumentIngestionService documentIngestionService;
    private final JdbcTemplate jdbcTemplate;


    // method to upload and parse document
    public DocumentResponseDto uploadAndProcess(MultipartFile file) {
        String fileName= file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        String contentType= file.getContentType() != null ? file.getContentType() : "application/octat-stream";

        //document meta data create
        DocumentMetadata documentMetadata = DocumentMetadata
                .builder()
                .filename(fileName)
                .contentType(contentType)
                .status(DocumentStatus.UPLOADING)
                .fileSize(file.getSize())
                .createdAt(LocalDateTime.now())
                .build();


        // save the document meta data
        documentMetadata = documentMetadataRepo.save(documentMetadata);

        // parse the file
        List<Document> parseDocs = documentParserService.parse(file);

        // in just service
        int chunksCreated = documentIngestionService.ingest(documentMetadata, parseDocs);

//        documentMetadata.setTotalChunks(chunksCreated);
        // save the document meta data
//        documentMetadata = documentMetadataRepo.save(documentMetadata);

        return DocumentResponseDto.builder()
                .id(documentMetadata.getId())
                .filename(documentMetadata.getFilename())
                .fileSize(documentMetadata.getFileSize())
                .chunksCreated(chunksCreated)
                .status(documentMetadata.getStatus())
                .message("document successfully processed and indexed. ")
                .build();


    }
}
