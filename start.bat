@echo off
echo ========================================
echo    AI Agent 启动脚本
echo ========================================
echo.

echo [1/4] 启动 Docker 容器 (MySQL, Milvus, Ollama)...
docker-compose up -d
echo.

echo [2/4] 等待服务启动 (约30秒)...
timeout /t 30 /nobreak
echo.

echo [3/4] 拉取 Ollama 模型 (qwen2.5 和 nomic-embed-text)...
docker exec ai-agent-ollama ollama pull qwen2.5
docker exec ai-agent-ollama ollama pull nomic-embed-text
echo.

echo [4/4] 启动 Spring Boot 应用...
echo 正在编译，请稍候...
set JAVA_HOME=C:\Users\Administrator\.jdks\graalvm-jdk-17.0.12
call mvnw.cmd spring-boot:run

pause
