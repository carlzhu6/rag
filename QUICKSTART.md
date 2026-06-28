# ⚡ 快速入门

## 5 分钟跑起来

### 前提条件
- ✅ Docker Desktop 已启动
- ✅ Java 17+ 已安装

### 启动命令

```bash
# 进入项目目录
cd D:\code\vscodetest\ai-agent

# 一键启动
start.bat
```

### 手动启动（如果脚本失败）

```bash
# 1. 启动服务
docker-compose up -d

# 2. 等待 30 秒后下载模型
docker exec ai-agent-ollama ollama pull qwen2.5:1.5b
docker exec ai-agent-ollama ollama pull nomic-embed-text

# 3. 启动应用
set JAVA_HOME=C:\Users\Administrator\.jdks\graalvm-jdk-17.0.12
mvn spring-boot:run
```

### 访问应用

打开浏览器：**http://localhost:8080**

---

## 使用流程

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  注册账号 │ →  │ 上传文档  │ →  │ 创建对话  │ →  │ 开始问答  │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
```

1. **注册** — 输入用户名和密码
2. **上传** — 点击「📚 知识库管理」上传文档
3. **创建** — 点击「+」新建对话
4. **问答** — 输入问题，AI 根据文档回答

---

## 服务端口

| 服务 | 端口 |
|------|------|
| Web 应用 | http://localhost:8080 |
| MySQL | localhost:3306 |
| Ollama | localhost:11434 |
| Milvus | localhost:19530 |
| MinIO 控制台 | http://localhost:9001 |

---

## 停止服务

```bash
docker-compose down
```

## 清理数据

```bash
docker-compose down -v  # 删除所有数据卷
```
