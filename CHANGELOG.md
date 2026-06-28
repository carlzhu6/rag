# 📋 更新日志

## [1.0.0] - 2026-06-28

### ✨ 新增功能

- **用户系统**
  - 用户注册/登录
  - JWT Token 认证
  - 多用户隔离

- **对话管理**
  - 创建/删除对话
  - 对话历史记录
  - 自动命名对话

- **知识库**
  - 文档上传（PDF/Word/TXT/Markdown）
  - 自动解析分块
  - Embedding 向量化
  - Milvus 存储检索

- **RAG 问答**
  - 基于知识库的智能问答
  - 多轮对话支持
  - 相似度检索 Top 5

- **Web 界面**
  - 响应式聊天 UI
  - 文档管理弹窗
  - 实时状态显示

- **基础设施**
  - Docker Compose 一键部署
  - MySQL 数据持久化
  - Milvus 向量存储
  - Ollama 本地 LLM

### 📦 技术栈

- Spring Boot 3.2.5
- LangChain4j 0.31.0
- MyBatis-Plus 3.5.6
- Milvus 2.4.0
- Ollama + Qwen2.5

---

## 未来计划

- [ ] SSE 流式输出（打字机效果）
- [ ] 文档在线预览
- [ ] 对话导出（PDF/Markdown）
- [ ] 多模型切换
- [ ] 管理员后台
- [ ] API 限流
- [ ] 文档自动更新
