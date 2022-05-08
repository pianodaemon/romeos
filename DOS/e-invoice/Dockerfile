FROM ubuntu:bionic AS builder
MAINTAINER eplauchu@immortalcrab.com

RUN apt-get update

RUN apt-get install -y openjdk-11-jdk default-jdk \
    maven \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME="/usr/lib/jvm/default-java"

COPY service /service

# Gear up the jars to be included into the fat jar
WORKDIR /service
RUN mvn install

# Begin of the fat jar creation
WORKDIR /service/server

# This trick makes possible the fat jar
RUN mkdir -p .mvn
RUN tar -xvf mvnw.tar -C .mvn
RUN ./mvnw clean package

FROM adoptopenjdk:11-jdk-hotspot-focal

ENV APP_DIR=/
# Copy the jar to the production image from the builder stage.
COPY --from=builder /service/server/target/web-server-DANCE_WITH_WOLVES.jar $APP_DIR/server-fat.jar
COPY scripts/run_service.sh $APP_DIR
WORKDIR $APP_DIR

EXPOSE 8080

CMD ["/run_service.sh"]
