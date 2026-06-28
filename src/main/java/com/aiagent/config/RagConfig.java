package com.aiagent.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@Configuration
public class RagConfig {

    @Value("${ai-agent.milvus.host}")
    private String milvusHost;

    @Value("${ai-agent.milvus.port}")
    private int milvusPort;

    @Value("${ai-agent.milvus.collection-name}")
    private String collectionName;

    @Value("${ai-agent.milvus.dimension}")
    private int dimension;

    @Value("${langchain4j.ollama.chat-model.base-url}")
    private String ollamaBaseUrl;

    @Value("${langchain4j.ollama.chat-model.model-name}")
    private String modelName;

    @Value("${ai-agent.document.chunk-size}")
    private int chunkSize;

    @Value("${ai-agent.document.chunk-overlap}")
    private int chunkOverlap;

    /**
     * Ollama 聊天模型
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(modelName)
                .temperature(0.7)
                .build();
    }

    /**
     * Ollama 嵌入模型
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName("nomic-embed-text")
                .build();
    }

    /**
     * Milvus 向量存储
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return MilvusEmbeddingStore.builder()
                .host(milvusHost)
                .port(milvusPort)
                .collectionName(collectionName)
                .dimension(dimension)
                .build();
    }

    /**
     * 文档解析器 (支持 PDF, DOCX, TXT, MD 等)
     */
    @Bean
    public DocumentParser documentParser() {
        return new ApacheTikaDocumentParser();
    }

    /**
     * 文档分割器
     */
    @Bean
    public DocumentSplitter documentSplitter() {
        return DocumentSplitters.recursive(chunkSize, chunkOverlap);
    }

    /**
     * RAG 内容检索器
     */
    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                              EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.5)
                .build();
    }

    /**
     * 知识助手接口 - LangChain4j AiServices
     */
    public interface KnowledgeAssistant {

        @SystemMessage("""
                你是一个智能助手。请优先根据提供的知识库内容回答用户的问题。
                如果知识库中有相关内容，请基于知识库内容回答。
                如果知识库中没有相关内容，请运用你自己的知识来回答，并说明"（以下回答基于通用知识）"。
                回答要简洁、准确、友好。
                """)
        String chat(String userMessage);
    }
}
