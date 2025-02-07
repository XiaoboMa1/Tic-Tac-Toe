# 使用官方 OpenJDK 作为基础镜像
FROM openjdk:17-jdk-slim AS build

# 设置工作目录
WORKDIR /app

# 复制 Maven 配置和源码
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# 赋予 Maven Wrapper 执行权限
RUN chmod +x mvnw

# 下载 Maven 依赖
RUN ./mvnw dependency:go-offline -B

# 复制整个项目源码
COPY src/ src/

# 编译并打包 Spring Boot 应用
RUN ./mvnw clean package -DskipTests

# 使用更轻量的 OpenJDK 作为最终运行镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制打包好的 .jar 文件到容器
COPY --from=build /app/target/*.jar app.jar

# 运行 Spring Boot 应用
CMD ["java", "-jar", "app.jar"]
