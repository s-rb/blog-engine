# Stage 1: Build
FROM maven:3.8.3-openjdk-11-slim AS build

ENV HOME=/home/app

RUN mkdir -p $HOME
WORKDIR $HOME
ADD pom.xml $HOME
RUN mvn verify --fail-never

COPY src ./src

RUN mvn package -DskipTests

# Stage 2: Create a lightweight image to run the application
FROM eclipse-temurin:11-jre
LABEL maintainer="Roman Surkov surkoff.com@gmail.com"

# Устанавливаем шрифты (правильные имена для Ubuntu)
RUN apt-get update && apt-get install -y \
    fontconfig \
    fonts-dejavu \
    fonts-dejavu-extra \
    && rm -rf /var/lib/apt/lists/*

# Включаем headless
ENV JAVA_OPTS="-Djava.awt.headless=true"

COPY --from=build /home/app/target/blog-engine-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]