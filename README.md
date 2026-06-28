# 🤖 AI 智能客服 — 基于 RAG 的知识库问答系统

> Spring Boot + LangChain4j + Ollama + Milvus 全栈 AI Agent 项目

---

## 📖 项目简介

这是一个基于 **RAG（Retrieval-Augmented Generation，检索增强生成）** 技术的智能客服系统。用户可以上传文档到知识库，系统会自动解析、分块、向量化存储，然后通过 AI 大模型根据知识库内容回答用户问题。

### 🎯 核心特性

| 特性 | 说明 |
|------|------|
| 📚 知识库管理 | 支持上传 PDF、Word、TXT、Markdown 文档，自动解析入库 |
| 🧠 RAG 问答 | 基于向量检索 + LLM 生成，回答准确且可溯源 |
| 💬 多轮对话 | 支持上下文记忆的多轮对话 |
| 👤 用户系统 | 注册/登录，JWT 认证，多用户隔离 |
| 🎨 Web 界面 | 内置聊天 UI，开箱即用 |
| 🔒 本地部署 | 数据不出本地，隐私安全 |

---

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│                        用户浏览器                            │
│          https://gallstone-sacrament-mandatory.ngrok-free.dev │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP
┌──────────────────────────▼──────────────────────────────────┐
│                    Spring Boot 3.2                          │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────────┐ │
│  │ Auth 模块   │  │ Chat 模块   │  │ Document 模块          │ │
│  │ • 注册登录  │  │ • 多轮对话  │  │ • 文档上传             │ │
│  │ • JWT 认证  │  │ • 历史记录  │  │ • 解析分块             │ │
│  └──────┬─────┘  └──────┬─────┘  │ • 向量化存储           │ │
│         │               │        └───────────┬─────────────┘ │
│         │               │                    │               │
│  ┌──────▼───────────────▼────────────────────▼─────────────┐ │
│  │                    RAG 核心引擎                          │ │
│  │  ┌──────────┐  ┌──────────┐  ┌────────────────────────┐ │ │
│  │  │ 检索      │  │ 增强      │  │ 生成                   │ │ │
│  │  │ Retrieve │→ │ Augment  │→ │ Generate               │ │ │
│  │  └──────────┘  └──────────┘  └────────────────────────┘ │ │
│  │                   LangChain4j                           │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────┬──────────────────────────────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
    ┌─────▼─────┐   ┌─────▼─────┐   ┌─────▼─────┐
    │   MySQL   │   │  Milvus   │   │  Ollama   │
    │  业务数据  │   │  向量数据  │   │  Qwen2.5  │
    └───────────┘   └───────────┘   └───────────┘
```

### 技术栈详解

| 层次 | 技术 | 版本 | 用途 |
|------|------|------|------|
| **后端框架** | Spring Boot | 3.2.5 | Web 应用框架 |
| **AI 框架** | LangChain4j | 0.31.0 | RAG、Embedding、Chat 集成 |
| **大语言模型** | Qwen2.5 (通义千问) | 1.5B | 中文问答生成 |
| **嵌入模型** | nomic-embed-text | - | 文本向量化 |
| **向量数据库** | Milvus | 2.4.0 | 向量存储与检索 |
| **业务数据库** | MySQL | 8.0 | 用户、对话、文档元数据 |
| **ORM 框架** | MyBatis-Plus | 3.5.6 | 数据库操作 |
| **认证** | Spring Security + JWT | - | 用户认证授权 |
| **文档解析** | Apache Tika | - | PDF/Word/TXT 解析 |
| **前端** | 原生 HTML/CSS/JS | - | 轻量级聊天界面 |

---

## 📁 项目结构

```
ai-agent/
├── docker-compose.yml              # Docker 容器编排
├── pom.xml                         # Maven 依赖配置
├── start.bat                       # Windows 一键启动脚本
├── README.md                       # 项目说明文档
│
├── src/
│   ├── main/
│   │   ├── java/com/aiagent/
│   │   │   ├── AiAgentApplication.java      # 启动类
│   │   │   │
│   │   │   ├── config/                       # 配置类
│   │   │   │   ├── SecurityConfig.java       # Spring Security + JWT
│   │   │   │   └── RagConfig.java            # LangChain4j + RAG 配置
│   │   │   │
│   │   │   ├── controller/                   # API 控制器
│   │   │   │   ├── AuthController.java       # 认证接口
│   │   │   │   ├── ChatController.java       # 对话接口
│   │   │   │   └── DocumentController.java   # 文档接口
│   │   │   │
│   │   │   ├── entity/                       # 数据实体
│   │   │   │   ├── User.java                 # 用户
│   │   │   │   ├── Conversation.java         # 对话
│   │   │   │   ├── ChatMessage.java          # 消息
│   │   │   │   └── KnowledgeDocument.java    # 知识文档
│   │   │   │
│   │   │   ├── mapper/                       # MyBatis Mapper
│   │   │   │   ├── UserMapper.java
│   │   │   │   ├── ConversationMapper.java
│   │   │   │   ├── ChatMessageMapper.java
│   │   │   │   └── KnowledgeDocumentMapper.java
│   │   │   │
│   │   │   ├── service/                      # 业务逻辑
│   │   │   │   ├── UserService.java          # 用户服务
│   │   │   │   ├── ChatService.java          # 对话服务
│   │   │   │   └── DocumentService.java      # 文档服务
│   │   │   │
│   │   │   ├── dto/                          # 数据传输对象
│   │   │   │   ├── ApiResponse.java          # 统一响应
│   │   │   │   ├── LoginRequest.java         # 登录请求
│   │   │   │   ├── RegisterRequest.java      # 注册请求
│   │   │   │   └── ChatRequest.java          # 对话请求
│   │   │   │
│   │   │   └── util/                         # 工具类
│   │   │       └── JwtUtil.java              # JWT 工具
│   │   │
│   │   └── resources/
│   │       ├── application.yml               # 应用配置
│   │       ├── db/
│   │       │   └── schema.sql                # 数据库初始化脚本
│   │       └── static/                       # 前端文件
│   │           ├── index.html                # 主页面
│   │           ├── css/style.css             # 样式
│   │           └── js/app.js                 # 交互逻辑
│   │
│   └── test/                                 # 测试代码
│
└── uploads/                                  # 文档上传目录（运行时生成）
```

---

## 🚀 快速开始

### 前置要求

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| Docker Desktop | 最新版 | 运行依赖服务 |
| Java JDK | 17+ | 编译运行 Spring Boot |
| Maven | 3.8+ | 构建项目（可选，可用 mvnw） |
| 磁盘空间 | 10GB+ | Docker 镜像 + AI 模型 |
| 内存 | 8GB+ | 建议 16GB 以获得更好体验 |

### 方式一：一键启动（推荐）

```bash
# 1. 进入项目目录
cd D:\code\vscodetest\ai-agent

# 2. 双击运行 start.bat
# 或在命令行执行：
start.bat
```

脚本会自动：
1. 启动 Docker 容器（MySQL、Milvus、Ollama）
2. 下载 AI 模型（Qwen2.5、nomic-embed-text）
3. 编译并启动 Spring Boot 应用

### 方式二：手动启动

#### 第 1 步：启动 Docker 服务

```bash
cd D:\code\vscodetest\ai-agent
docker-compose up -d
```

等待所有容器启动完成（约 30-60 秒）：

```bash
# 检查容器状态
docker ps

# 应该看到以下容器：
# ai-agent-mysql      (healthy)
# ai-agent-milvus     (healthy)
# ai-agent-ollama     (up)
# ai-agent-etcd       (up)
# ai-agent-minio      (healthy)
```

#### 第 2 步：下载 AI 模型

```bash
# 下载中文对话模型（约 1GB）
docker exec ai-agent-ollama ollama pull qwen2.5:1.5b

# 下载嵌入模型（约 274MB）
docker exec ai-agent-ollama ollama pull nomic-embed-text

# 验证模型已下载
docker exec ai-agent-ollama ollama list
```

#### 第 3 步：启动 Spring Boot 应用

```bash
# 设置 Java 环境
set JAVA_HOME=C:\Users\Administrator\.jdks\graalvm-jdk-17.0.12

# 编译并启动
mvn spring-boot:run
```

#### 第 4 步：访问应用

打开浏览器访问：**https://gallstone-sacrament-mandatory.ngrok-free.dev**

### 方式三：公网访问（ngrok）

当前已配置 ngrok 公网访问：

**🔗 访问地址：https://gallstone-sacrament-mandatory.ngrok-free.dev**

```bash
# 自行搭建时的操作步骤：
# 1. 下载 ngrok → https://ngrok.com/download
# 2. 配置 authtoken
ngrok config add-authtoken <your-token>
# 3. 启动穿透
ngrok http 8080
```

---

## 📚 使用教程

### 1️⃣ 注册与登录

首次访问会看到登录页面：

1. 点击 **「注册」** 标签
2. 输入用户名、密码、昵称（选填）
3. 点击注册按钮
4. 注册成功后自动登录进入主界面

### 2️⃣ 上传知识库文档

1. 点击左侧 **「📚 知识库管理」** 按钮
2. 在弹窗中点击上传区域，或直接拖拽文件
3. 支持的文档格式：
   - 📕 PDF 文件
   - 📘 Word 文档（.doc / .docx）
   - 📝 纯文本（.txt）
   - 📗 Markdown（.md）
4. 等待文档状态变为 **「就绪」**（首次可能需要几秒）

**文档处理流程：**
```
上传文档 → Apache Tika 解析 → 文本分块(500字/块) → Embedding 向量化 → 存入 Milvus
```

### 3️⃣ 创建对话

1. 点击左侧 **「+」** 按钮创建新对话
2. 对话列表会显示所有历史对话
3. 点击对话可以切换

### 4️⃣ 开始问答

1. 在输入框输入问题
2. 按 **Enter** 或点击 **「发送」** 按钮
3. AI 会根据知识库内容生成回答
4. 支持多轮对话，AI 会记住上下文

**问答流程：**
```
用户提问 → Embedding 向量化 → Milvus 相似度检索 → 取 Top 5 相关片段
    → 拼接 Prompt → 调用 Qwen2.5 生成回答 → 返回用户
```

### 5️⃣ 管理对话

- **切换对话**：点击左侧对话列表
- **删除对话**：悬停对话，点击 **「✕」** 按钮
- **自动命名**：首条消息后，对话标题自动更新

---

## 🔌 API 接口文档

### 认证接口

#### 注册
```http
POST /api/auth/register
Content-Type: application/json

{
    "username": "testuser",
    "password": "123456",
    "nickname": "测试用户"
}

# 响应
{
    "code": 200,
    "message": "success",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "userId": 1,
        "username": "testuser",
        "nickname": "测试用户"
    }
}
```

#### 登录
```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "testuser",
    "password": "123456"
}

# 响应同注册
```

### 对话接口

> 以下接口需要在 Header 中携带 Token：
> `Authorization: Bearer <token>`

#### 创建对话
```http
POST /api/chat/conversations
Content-Type: application/json

{
    "title": "我的第一个对话"  // 可选
}
```

#### 获取对话列表
```http
GET /api/chat/conversations
```

#### 获取对话历史
```http
GET /api/chat/conversations/{conversationId}/messages
```

#### 发送消息
```http
POST /api/chat/send
Content-Type: application/json

{
    "conversationId": 1,
    "message": "什么是人工智能？"
}

# 响应
{
    "code": 200,
    "data": {
        "reply": "人工智能（AI）是计算机科学的一个分支..."
    }
}
```

#### 删除对话
```http
DELETE /api/chat/conversations/{conversationId}
```

### 文档接口

#### 上传文档
```http
POST /api/documents/upload
Content-Type: multipart/form-data

file: <文件二进制>

# 响应
{
    "code": 200,
    "data": {
        "id": 1,
        "fileName": "AI技术白皮书.pdf",
        "fileType": "pdf",
        "fileSize": 1048576,
        "status": "READY"
    }
}
```

#### 获取文档列表
```http
GET /api/documents
```

#### 删除文档
```http
DELETE /api/documents/{docId}
```

---

## ⚙️ 配置说明

### application.yml 核心配置

```yaml
# 服务端口
server:
  port: 8080

# MySQL 数据库
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_agent
    username: aiagent
    password: aiagent123

# LangChain4j + Ollama
langchain4j:
  ollama:
    chat-model:
      model-name: qwen2.5:1.5b    # 对话模型
      base-url: http://localhost:11434
      temperature: 0.7              # 创造性（0-1）

# 自定义配置
ai-agent:
  milvus:
    host: localhost
    port: 19530
    collection-name: knowledge_base
    dimension: 768                  # 向量维度（nomic-embed-text 输出 768 维）

  document:
    upload-dir: ./uploads           # 上传目录
    chunk-size: 500                 # 分块大小（字符）
    chunk-overlap: 50               # 分块重叠

  jwt:
    secret: YourSecretKey           # JWT 密钥（生产环境请修改）
    expiration: 86400000            # Token 有效期（毫秒）
```

### 更换 AI 模型

如果需要更好的效果，可以下载更大的模型：

```bash
# 下载完整版 Qwen2.5（约 4.7GB，效果更好）
docker exec ai-agent-ollama ollama pull qwen2.5

# 然后修改 application.yml
# model-name: qwen2.5
```

常用模型对比：

| 模型 | 大小 | 中文能力 | 推理速度 | 推荐场景 |
|------|------|----------|----------|----------|
| qwen2.5:0.5b | 395MB | ⭐⭐ | ⭐⭐⭐⭐⭐ | 测试/演示 |
| qwen2.5:1.5b | 986MB | ⭐⭐⭐ | ⭐⭐⭐⭐ | 开发/轻量 |
| qwen2.5:7b | 4.7GB | ⭐⭐⭐⭐⭐ | ⭐⭐ | 生产（需 GPU） |
| qwen2.5:14b | 9GB | ⭐⭐⭐⭐⭐ | ⭐ | 高质量（需 GPU） |

---

## 🛠️ 常见问题

### Q1: Docker 容器启动失败

```bash
# 检查 Docker 是否运行
docker info

# 查看容器日志
docker logs ai-agent-mysql
docker logs ai-agent-milvus

# 重启容器
docker-compose down
docker-compose up -d
```

### Q2: Milvus 启动慢

Milvus 首次启动需要 60-90 秒初始化，请耐心等待。

```bash
# 检查 Milvus 状态
curl http://localhost:9091/healthz

# 返回 OK 表示就绪
```

### Q3: 模型下载失败

网络不稳定可能导致下载中断，重试即可：

```bash
# 重试下载
docker exec ai-agent-ollama ollama pull qwen2.5:1.5b

# Ollama 支持断点续传
```

### Q4: 端口被占用

如果 3306、11434、19530 端口被占用，修改 `docker-compose.yml`：

```yaml
ports:
  - "3307:3306"  # 改为其他端口
```

同时修改 `application.yml` 中对应的连接地址。

### Q5: 内存不足

Ollama 运行模型需要较多内存：

| 模型 | 最低内存 | 推荐内存 |
|------|----------|----------|
| 1.5B | 4GB | 8GB |
| 7B | 8GB | 16GB |
| 14B | 16GB | 32GB |

### Q6: 应用启动报错

```bash
# 清理重新编译
mvn clean compile

# 查看详细错误
mvn spring-boot:run -X
```

---

## 📊 RAG 工作原理

```
                         ┌─────────────────────────────────────┐
                         │           文档处理流程                │
                         └─────────────────────────────────────┘
                                          │
                         ┌────────────────▼───────────────────┐
                         │      1. 文档上传 (PDF/Word/TXT)     │
                         └────────────────┬───────────────────┘
                                          │
                         ┌────────────────▼───────────────────┐
                         │      2. Apache Tika 解析文本        │
                         └────────────────┬───────────────────┘
                                          │
                         ┌────────────────▼───────────────────┐
                         │      3. 文本分块 (500字/块)         │
                         │         重叠 50 字保持连贯          │
                         └────────────────┬───────────────────┘
                                          │
                         ┌────────────────▼───────────────────┐
                         │      4. Embedding 向量化            │
                         │         nomic-embed-text           │
                         └────────────────┬───────────────────┘
                                          │
                         ┌────────────────▼───────────────────┐
                         │      5. 存入 Milvus 向量数据库      │
                         └────────────────────────────────────┘


                         ┌─────────────────────────────────────┐
                         │           问答流程                   │
                         └─────────────────────────────────────┘
                                          │
                         ┌────────────────▼───────────────────┐
                         │      1. 用户提问                    │
                         └────────────────┬───────────────────┘
                                          │
                         ┌────────────────▼───────────────────┐
                         │      2. 问题 Embedding 向量化       │
                         └────────────────┬───────────────────┘
                                          │
                         ┌────────────────▼───────────────────┐
                         │      3. Milvus 相似度检索           │
                         │         返回 Top 5 相关片段         │
                         └────────────────┬───────────────────┘
                                          │
                         ┌────────────────▼───────────────────┐
                         │      4. 拼接 Prompt                 │
                         │         系统指令 + 检索结果 + 问题   │
                         └────────────────┬───────────────────┘
                                          │
                         ┌────────────────▼───────────────────┐
                         │      5. 调用 Qwen2.5 生成回答       │
                         └────────────────┬───────────────────┘
                                          │
                         ┌────────────────▼───────────────────┐
                         │      6. 返回用户                    │
                         └────────────────────────────────────┘
```

---

## 🔧 开发指南

### 本地开发环境搭建

```bash
# 1. 克隆项目
git clone <repo-url>

# 2. 安装依赖
mvn clean install

# 3. 启动 Docker 服务
docker-compose up -d

# 4. 下载模型
docker exec ai-agent-ollama ollama pull qwen2.5:1.5b
docker exec ai-agent-ollama ollama pull nomic-embed-text

# 5. 运行应用
mvn spring-boot:run
```

### 代码规范

- 实体类使用 Lombok `@Data` 注解
- 统一返回 `ApiResponse<T>` 包装
- 使用 MyBatis-Plus 简化数据库操作
- 配置类集中管理，便于维护

### 扩展建议

1. **添加 SSE 流式输出** — 实现打字机效果
2. **接入更多文档格式** — Excel、PPT 等
3. **添加文档管理后台** — 管理员查看所有文档
4. **集成更多 LLM** — 支持 OpenAI、Claude 等
5. **添加对话导出** — 导出为 PDF/Markdown

---

## 🧠 RAG 问答策略

系统采用**混合问答策略**：

| 场景 | 回答来源 | 说明 |
|------|----------|------|
| 知识库有相关内容 | 知识库 | 基于检索到的文档片段生成回答 |
| 知识库无相关内容 | Qwen 通用知识 | 使用模型自身知识回答，并标注"（以下回答基于通用知识）" |

**系统提示词：**
```
你是一个智能助手。请优先根据提供的知识库内容回答用户的问题。
如果知识库中有相关内容，请基于知识库内容回答。
如果知识库中没有相关内容，请运用你自己的知识来回答，并说明"（以下回答基于通用知识）"。
回答要简洁、准确、友好。
```

---

## 📄 许可证

MIT License

---

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [LangChain4j](https://github.com/langchain4j/langchain4j)
- [Ollama](https://ollama.ai)
- [Milvus](https://milvus.io)
- [Qwen](https://github.com/QwenLM/Qwen)
