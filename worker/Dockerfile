FROM maven:3.10-eclipse-temurin-25-resolute AS builder
WORKDIR /build
COPY . .
ARG SKIP_TESTS=false
RUN --mount=type=cache,target=/root/.m2 mvn clean package -pl worker -am -Dmaven.test.skip.exec=${SKIP_TESTS}

FROM eclipse-temurin:25-jre-resolute
WORKDIR /app
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        fontconfig && \
    rm -rf /var/lib/apt/lists/*
COPY --from=builder /build/worker/target/*-exec.jar worker.jar
ENTRYPOINT ["java", "-jar", "worker.jar"]