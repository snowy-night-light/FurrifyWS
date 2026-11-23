FROM maven:3.10-eclipse-temurin-25-resolute AS builder
WORKDIR /build
COPY . .
ARG SKIP_TESTS=false
RUN --mount=type=cache,target=/root/.m2 mvn clean package -pl storage -am -Dmaven.test.skip.exec=${SKIP_TESTS}

FROM eclipse-temurin:25-jre-resolute
WORKDIR /app
COPY --from=builder /build/storage/target/*-exec.jar storage.jar
ENTRYPOINT ["java", "-jar", "storage.jar"]
