#Build stage
FROM 3.8.3-openjdk-17 AS build-env

ADD . /rfc3161timestampingserver

WORKDIR rfc3161timestampingserver

RUN mvn -U compile package assembly:single

# Run it
FROM openjdk:17

COPY --from=build-env /rfc3161timestampingserver/target/*-jar-with-dependencies.jar /app/rfc3161timestampingserver.jar

EXPOSE 7000

CMD ["java", "-jar", "/app/rfc3161timestampingserver.jar"]