FROM fabric8/java-jboss-openjdk8-jdk:1.5.1

ENV JAEGER_SERVICE_NAME=customer\
  JAEGER_SAMPLER_TYPE=const\
  JAEGER_SAMPLER_PARAM=1

EXPOSE 8080
COPY build/libs/demo-1.0.0-SNAPSHOT-fat.jar /deployments/
