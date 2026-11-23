FROM maven:3.10-eclipse-temurin-25-resolute AS builder
WORKDIR /build
COPY . .
ARG SKIP_TESTS=false
RUN --mount=type=cache,target=/root/.m2 mvn clean package -pl attachment -am -Dmaven.test.skip.exec=${SKIP_TESTS}

FROM eclipse-temurin:25-jre-resolute
WORKDIR /app
# ffmpeg: video thumbnails, decoding images cjxl can't read, pixel checks of conversions
# libjxl-tools: cjxl/djxl (JPEG XL conversion and fallbacks), optipng: lossless optimization of fallback PNGs
# libheif-examples (+ plugins): heif-dec for HEIC/HEIF/AVIF uploads
# libimage-exiftool-perl: exiftool, metadata of uploads stored before conversion
# webp: anim_dump/webpmux, frames and timings of animated WebP (ffmpeg can't decode those)
RUN apt-get update && apt-get install -y --no-install-recommends \
        ffmpeg fontconfig \
        libjxl-tools optipng \
        libheif-examples libheif-plugin-libde265 libheif-plugin-dav1d \
        libimage-exiftool-perl webp && \
    rm -rf /var/lib/apt/lists/*
COPY --from=builder /build/attachment/target/*-exec.jar attachment.jar
ENTRYPOINT ["java", "-jar", "attachment.jar"]
