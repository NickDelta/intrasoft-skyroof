FROM openjdk:8-jre-slim

COPY target/skyroof-api.jar app.jar

RUN groupadd skyroof && useradd -g users -G skyroof skyroof
USER skyroof

EXPOSE 8080

CMD java -jar app.jar