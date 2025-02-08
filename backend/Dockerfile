# Build stage: 使用 Maven 和 OpenJDK 17 进行编译
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Production stage: 使用更轻量的 OpenJDK 17 镜像运行 jar 包
FROM openjdk:17-slim
ARG PORT=8080
COPY --from=build /app/target/*.jar /app.jar
EXPOSE ${PORT}
CMD ["sh", "-c", "java -jar /app.jar --server.port=${PORT}"]
