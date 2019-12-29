FROM openjdk:11-slim-buster AS builder
RUN apt-get update       && \
    apt-get dist-upgrade
WORKDIR /build
# init gradle version defined by build and load dependencies
ADD gradle gradle
ADD *.gradle gradlew ./
RUN ./gradlew clean --no-daemon --refresh-dependencies -q
# build app
ADD src src
RUN ./gradlew build --no-daemon -q

FROM fabric8/java-alpine-openjdk11-jre
RUN apk --no-cache add ca-certificates
USER nobody
WORKDIR /deployments
COPY --from=builder /build/build/libs/*.jar ./app.jar
EXPOSE 8080
ENV JAVA_OPTIONS=""
ENTRYPOINT [ "./run-java.sh" ]
