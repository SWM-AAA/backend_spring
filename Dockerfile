# FROM gradle:8.1.1-jdk17-alpine as build

# ENV APP_HOME=/backend_spring

# WORKDIR $APP_HOME

# COPY build.gradle settings.gradle gradlew $APP_HOME

# COPY gradle $APP_HOME/gradle

# RUN chmod +x gradlew

# RUN ./gradlew build || return 0

# COPY src $APP_HOME/src

# RUN ./gradlew clean build -x asciidoctor -x test -x processResources

# FROM openjdk:17.0.2-jdk

# ARG ARTIFACT_NAME=app.jar
# ARG JAR_FILE_PATH=build/libs/demo-0.0.1-SNAPSHOT.jar

# WORKDIR $APP_HOME

# ENV SPRING_PROFILES_ACTIVE=dev

# #COPY --from=build /apps/build/libs/demo-0.0.1-SNAPSHOT.jar app.jar
# COPY --from=build $APP_HOME/$JAR_FILE_PATH $ARTIFACT_NAME

# EXPOSE 8080

# #ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
# ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"]

FROM openjdk:17.0.2-jdk

ENV APP_HOME=/backend_spring

ARG JAR_FILE_PATH=build/libs/zeppy-0.0.1-SNAPSHOT.jar

WORKDIR $APP_HOME

COPY $JAR_FILE_PATH app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]