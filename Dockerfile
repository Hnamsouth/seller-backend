FROM maven:3.8.7 AS builder

WORKDIR /server/
ADD ./vipo-seller-backend /server/
#RUN mvn clean install
RUN --mount=type=cache,target=/root/.m2 mvn clean install -DskipTests

############

#FROM openjdk:17-ea-slim
#FROM openjdk:17-slim-buster
FROM eclipse-temurin:17-jre-alpine
#FROM eclipse-temurin:17-jdk-alpine-3.21
# Install font dependencies
RUN apk add --no-cache \
    fontconfig \
    freetype


WORKDIR /opt/app

COPY --from=builder /server/target/seller-service-0.0.1-SNAPSHOT.jar /opt/app/seller-service-0.0.1-SNAPSHOT.jar
COPY ./application-prod.yml /opt/app/application-prod.yml

EXPOSE 8081

ENTRYPOINT ["java","-Duser.timezone=Asia/Ho_Chi_Minh","-jar","seller-service-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod","--spring.config.location=application-prod.yml"]