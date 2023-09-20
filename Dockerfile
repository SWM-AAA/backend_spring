FROM openjdk:17.0.2-jdk

ENV APP_HOME=/backend_spring

ARG JAR_FILE_PATH=build/libs/zeppy-0.0.1-SNAPSHOT.jar

WORKDIR $APP_HOME

COPY $JAR_FILE_PATH app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]




