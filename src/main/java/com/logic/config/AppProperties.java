package com.logic.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private RagProperties rag = new RagProperties();
    private CoreProperties cors= new CoreProperties();


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CoreProperties{
        private String allowedOrigins = "*";
        private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
        private String allowedHeaders = "*";
    }




    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RagProperties{
        private int chunkSize = 600;
        private int minChunkSizeChars = 350;
        private int minChunkLengthToEmbed = 5;
        private int maxNumChunks = 1000;
//        private int chunkOverlap = 100;
        private int topk = 5;
        private double similarityThreshold = 0.0;

    }

}
