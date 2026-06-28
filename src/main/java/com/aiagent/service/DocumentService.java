package com.aiagent.service;

import com.aiagent.config.RagConfig;
import com.aiagent.entity.KnowledgeDocument;
import com.aiagent.mapper.KnowledgeDocumentMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final KnowledgeDocumentMapper documentMapper;
    private final DocumentParser documentParser;
    private final DocumentSplitter documentSplitter;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Value("${ai-agent.document.upload-dir}")
    private String uploadDir;

    public KnowledgeDocument uploadDocument(MultipartFile file, Long userId) throws IOException {
        // 创建上传目录（使用绝对路径）
        String absolutePath = new File(uploadDir).getAbsolutePath();
        log.info("上传目录绝对路径: {}", absolutePath);

        Path uploadPath = Paths.get(absolutePath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("已创建上传目录: {}", uploadPath);
        }

        // 保存文件
        String originalFilename = file.getOriginalFilename();
        String fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        String savedFilename = UUID.randomUUID().toString() + "." + fileType;
        Path filePath = uploadPath.resolve(savedFilename);
        file.transferTo(filePath.toFile());

        // 记录文档信息
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setFileName(originalFilename);
        doc.setFileType(fileType);
        doc.setFileSize(file.getSize());
        doc.setStatus("PARSING");
        doc.setUploadedBy(userId);
        doc.setCreatedAt(LocalDateTime.now());
        documentMapper.insert(doc);

        // 异步处理文档（解析、分块、嵌入）
        try {
            processDocument(filePath.toFile(), doc.getId());
            doc.setStatus("READY");
        } catch (Exception e) {
            log.error("文档处理失败: {}", originalFilename, e);
            doc.setStatus("FAILED");
        }
        documentMapper.updateById(doc);

        return doc;
    }

    private void processDocument(File file, Long docId) {
        try {
            // 1. 解析文档
            Document document = documentParser.parse(new java.io.FileInputStream(file));
            document.metadata().put("documentId", String.valueOf(docId));

            // 2. 分割文档
            List<TextSegment> segments = documentSplitter.split(document);

            // 3. 生成嵌入并存储到 Milvus
            EmbeddingStoreIngestor.builder()
                    .documentSplitter(documentSplitter)
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build()
                    .ingest(document);

            log.info("文档处理完成，文档ID: {}, 分块数: {}", docId, segments.size());
        } catch (Exception e) {
            throw new RuntimeException("文档处理失败", e);
        }
    }

    public List<KnowledgeDocument> getDocuments(Long userId) {
        return documentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeDocument>()
                        .eq(KnowledgeDocument::getUploadedBy, userId)
                        .orderByDesc(KnowledgeDocument::getCreatedAt)
        );
    }

    public void deleteDocument(Long docId, Long userId) {
        KnowledgeDocument doc = documentMapper.selectById(docId);
        if (doc != null && doc.getUploadedBy().equals(userId)) {
            documentMapper.deleteById(docId);
            // 注意：这里没有从 Milvus 删除向量，实际项目中需要处理
        }
    }
}
