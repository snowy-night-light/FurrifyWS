FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /build
COPY . .
ARG SKIP_TESTS=false
RUN --mount=type=cache,target=/root/.m2 mvn clean package -pl attachment -am -Dmaven.test.skip.exec=${SKIP_TESTS}

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN apt-get update && apt-get install -y ffmpeg fontconfig && rm -rf /var/lib/apt/lists/*
COPY --from=builder /build/attachment/target/*-exec.jar attachment.jar
ENTRYPOINT ["java", "-jar", "attachment.jar"]
