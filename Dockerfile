# ==============================================================================
# Multi-stage Dockerfile cho TaskFlow Enterprise (Workflow-Manager-System)
# Phù hợp chạy trực tiếp trên Render, Railway, AWS ECS, hoặc Docker container
# ==============================================================================

# Giai đoạn 1: Build file JAR từ mã nguồn
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy cấu hình Maven Mirror (Google CDN) để tránh bị chặn 429 Too Many Requests trên Render
COPY maven-settings.xml /root/.m2/settings.xml

# Copy tệp pom.xml và tải dependencies (tận dụng Docker layer cache)
COPY pom.xml .
RUN mvn -s /root/.m2/settings.xml dependency:go-offline -B || true

# Copy toàn bộ mã nguồn và biên dịch đóng gói
COPY src ./src
RUN mvn -s /root/.m2/settings.xml clean package -DskipTests

# Giai đoạn 2: Tạo runtime image siêu nhẹ
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy artifact JAR từ build stage
COPY --from=build /app/target/*.jar app.jar

# Biến môi trường mặc định (Render tự động inject PORT=10000)
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]
