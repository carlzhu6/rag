package com.aiagent.controller;

import com.aiagent.dto.ApiResponse;
import com.aiagent.entity.KnowledgeDocument;
import com.aiagent.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ApiResponse<KnowledgeDocument> uploadDocument(HttpServletRequest request,
                                                          @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("收到文档上传请求: userId={}, fileName={}, fileSize={}", userId, file.getOriginalFilename(), file.getSize());

        if (userId == null) {
            log.error("userId 为空，认证可能失败");
            return ApiResponse.error(401, "未登录或认证失败");
        }

        try {
            KnowledgeDocument doc = documentService.uploadDocument(file, userId);
            log.info("文档上传成功: docId={}", doc.getId());
            return ApiResponse.success(doc);
        } catch (Exception e) {
            log.error("文档上传失败", e);
            return ApiResponse.error(500, "文档上传失败: " + e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<List<KnowledgeDocument>> getDocuments(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("获取文档列表: userId={}", userId);

        if (userId == null) {
            return ApiResponse.error(401, "未登录或认证失败");
        }

        List<KnowledgeDocument> documents = documentService.getDocuments(userId);
        return ApiResponse.success(documents);
    }

    @DeleteMapping("/{docId}")
    public ApiResponse<Void> deleteDocument(HttpServletRequest request,
                                             @PathVariable Long docId) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("删除文档: userId={}, docId={}", userId, docId);

        if (userId == null) {
            return ApiResponse.error(401, "未登录或认证失败");
        }

        documentService.deleteDocument(docId, userId);
        return ApiResponse.success();
    }
}
