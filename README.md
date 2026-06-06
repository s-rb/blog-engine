# Blog Engine Backend

![Java](https://img.shields.io/badge/-Java-05122A?style=flat&logo=Java&logoColor=FFA518) ![WebService](https://img.shields.io/badge/-WebService-05122A?style=flat) ![Spring](https://img.shields.io/badge/-Spring-05122A?style=flat&logo=Spring&logoColor=71b23c) ![Springboot](https://img.shields.io/badge/-SpringBoot-05122A?style=flat&logo=Springboot&logoColor=71b23c) ![JUnit4](https://img.shields.io/badge/-JUnit4-05122A?style=flat) ![Maven](https://img.shields.io/badge/-Maven-05122A?style=flat&logo=apachemaven&logoColor=fffffb) ![Postgresql](https://img.shields.io/badge/-Postgres-05122A?style=flat&logo=postgresql&logoColor=fffffb) ![Captcha](https://img.shields.io/badge/-Captcha-05122A?style=flat) ![Mail](https://img.shields.io/badge/-Spring_Mail-05122A?style=flat&logo=springboot) ![Lombok](https://img.shields.io/badge/-Lombok-05122A?style=flat&logo=lombok) ![JSoup](https://img.shields.io/badge/-JSoup-05122A?style=flat&logo=jsoup) ![Thymeleaf](https://img.shields.io/badge/-Thymeleaf-05122A?style=flat&logo=Thymeleaf) ![REST](https://img.shields.io/badge/-REST-05122A?style=flat)

[Try it here, click me!](http://blog.surkoff.com)

Java-based Spring Boot backend for a blog.

Packaged in a Docker container and uses a Postgres database, also running in a Docker container.
Startup is done using docker-compose.

Packaging in a Docker container uses multi-stage builds and minimal images to reduce the image size.

Database migration system Flyway is used for initial database setup.

Don't forget to specify your data - username, password, and database name in docker-compose.

The connection string, database, and user with password are specified in application.properties.

To start with default parameters, use the command, where the flag `--build` is for mandatory build, and `--force-recreate` is an option used in the docker-compose command to forcibly recreate all containers, even if the service configuration has not changed.
 
```shell
docker-compose up -d --build --force-recreate
```

![Изображение фронтэнда](images/FrontEnd.png)

## Main technologies:
* Java11
* Apache Maven
* Spring Framework (Using Spring Boot project)
* Postgres
* Git
* JUnit
* FlyWay
* Docker + Docker-Compose
* TestContainers

## Additional features:
* Hibernate
* Cage (CAptcha GEnerator)
* Jakarta Mail
* Jsoup library
* Lombok project
* Logback
* FasterXML Jackson
* SnakeYAML

Application is RESTful.

![Схема REST](images/Rest.png)


Designed with pattern MVC.

![Схема MVC](images/MVC.png)


Code covered by tests for 75% lines.

![Скрин JUnit](images/Junit.png)

Docker command to start postgres in docker
`docker run --name postgresql14-container -p 5432:5432 -e POSTGRES_PASSWORD=mypassword -e PGDATA=/var/lib/postgresql/data/pgdata -v "/home/myuser/data/postgres":/var/lib/postgresql/data -d --restart=unless-stopped postgres:14`

The final work at Skillbох's course "Java-developer".
