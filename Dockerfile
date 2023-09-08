FROM --platform=$BUILDPLATFORM eclipse-temurin:18-jdk-focal as builder

WORKDIR /app
COPY gradle/ gradle/
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY src src/

RUN ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:17.0.8.1_1-jre

ENV JAEGER_SERVICE_NAME=customer\
  JAEGER_SAMPLER_TYPE=const\
  JAEGER_SAMPLER_PARAM=1

EXPOSE 8080
COPY --from=builder /app/build/libs/demo-1.0.0-SNAPSHOT-fat.jar .
ENTRYPOINT ["java", "-jar", "demo-1.0.0-SNAPSHOT-fat.jar"]
