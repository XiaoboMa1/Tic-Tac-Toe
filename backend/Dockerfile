
# 构建阶段
FROM eclipse-temurin:17-jdk-slim AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-slim
ARG PORT=8080
ENV PORT=${PORT}
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
CMD ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT}"]
