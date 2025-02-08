# Build stage: 使用 Maven 3.8.7 和 OpenJDK 17 编译项目
FROM maven:3.8.7-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Production stage: 使用轻量级 OpenJDK 17 运行 jar 包
FROM openjdk:17-slim
WORKDIR /app

# 定义运行时端口，并将构建参数转换为环境变量
ARG PORT=8080
ENV PORT=${PORT}

# 明确指定 JAR 文件，避免通配符匹配不确定性
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar /app/app.jar

# 声明服务监听的端口（这里建议直接写数值）
EXPOSE 8080

# 运行 Spring Boot 应用，利用环境变量替换端口
CMD ["sh", "-c", "java -jar /app/app.jar --server.port=$PORT"]
