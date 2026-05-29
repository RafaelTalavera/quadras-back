# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
COPY .mvn ./.mvn
COPY mvnw ./
COPY mvnw.cmd ./
RUN chmod +x mvnw && ./mvnw -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

ENV PORT=8080
ENV JAVA_OPTS="-XX:+UseSerialGC -XX:InitialRAMPercentage=10 -XX:MaxRAMPercentage=55 -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=64m -Xss256k -XX:+UseStringDeduplication -Djava.awt.headless=true"

COPY --from=build /app/target/*-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -Dserver.port=${PORT} -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]
