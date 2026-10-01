FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /build
COPY . .
ARG SKIP_TESTS=false
RUN --mount=type=cache,target=/root/.m2 mvn clean package -pl gateway -am -Dmaven.test.skip.exec=${SKIP_TESTS}

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /build/gateway/target/*-exec.jar gateway.jar
ENTRYPOINT ["java", "-jar", "gateway.jar"]
