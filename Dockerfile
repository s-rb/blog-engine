# Stage 1: Build
FROM maven:3.8.3-openjdk-17 AS build

ENV HOME=/home/app

RUN mkdir -p $HOME
WORKDIR $HOME
ADD pom.xml $HOME
RUN mvn verify --fail-never

COPY src ./src

RUN mvn package -DskipTests

# Stage 2: Create a lightweight image to run the application
FROM openjdk:11

COPY --from=build /home/app/target/blog-engine-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]