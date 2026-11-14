package com.logic.dto;

import com.logic.entity.Enum.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentResponseDto {
    private UUID id;
    private String filename;
    private Long fileSize;
    private DocumentStatus status;
    private Integer chunksCreated;
    private String message;
}
