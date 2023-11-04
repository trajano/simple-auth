FROM --platform=$BUILDPLATFORM gradle AS build
WORKDIR /w
COPY . /w
RUN gradle bootJar

FROM --platform=$BUILDPLATFORM amazoncorretto:17-alpine-jdk AS extract
WORKDIR /w
COPY --from=build /w/build/libs/*.jar /w/app.jar
RUN java -Djarmode=layertools -jar /w/app.jar extract

FROM --platform=$BUILDPLATFORM alpine/curl AS download-otel-agent
RUN curl -sL -o /tmp/opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

FROM amazoncorretto:17-alpine-jdk
COPY --from=extract --link /w/dependencies/ /w/
COPY --from=extract --link /w/snapshot-dependencies/ /w/
COPY --from=extract --link /w/spring-boot-loader/ /w/
COPY --from=extract --link /w/application/ /w/
COPY --from=download-otel-agent /tmp/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar
WORKDIR /w
CMD [ "java", "-XX:MinRAMPercentage=60.0", "-XX:MaxRAMPercentage=60.0", "-XshowSettings:vm", "org.springframework.boot.loader.JarLauncher" ]
HEALTHCHECK CMD wget -O /dev/null http://localhost:8080/actuator/health || exit 1
