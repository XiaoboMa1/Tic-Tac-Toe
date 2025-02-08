# Build stage: 使用 Maven 3.9.7 和 OpenJDK 17 编译项目
FROM maven:3.9.9-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Production stage: 使用轻量级 OpenJDK 17 运行 jar 包
FROM openjdk:17-slim
ARG PORT=8080
ENV PORT=${PORT}
# 从 build 阶段复制 jar 文件到 /app/app.jar
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
CMD ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT}"]
