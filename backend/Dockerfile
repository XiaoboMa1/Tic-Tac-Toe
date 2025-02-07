# 使用官方 OpenJDK 作为基础镜像
FROM openjdk:17-jdk-slim AS build

# 设置工作目录
WORKDIR /app

# 复制 Maven 配置和源码
COPY .mvn/ .mvn
FROM maven:3.8.6-openjdk-11 AS build
WORKDIR /app
COPY . .
RUN mvn package -DskipTests

FROM openjdk:11-jre-slim
COPY --from=build /app/target/*.jar /app.jar
EXPOSE $PORT
CMD ["java", "-jar", "/app.jar", "--server.port=$PORT"]
