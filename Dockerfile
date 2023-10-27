FROM --platform=$BUILDPLATFORM gradle AS build
WORKDIR /w
COPY . /w
RUN gradle bootJar

FROM --platform=$BUILDPLATFORM amazoncorretto:17-alpine-jdk AS extract
WORKDIR /w
COPY --from=build /w/build/libs/*.jar /w/app.jar
RUN java -Djarmode=layertools -jar /w/app.jar extract

FROM amazoncorretto:17-alpine-jdk
COPY --from=extract --link /w/dependencies/ /w/
COPY --from=extract --link /w/snapshot-dependencies/ /w/
COPY --from=extract --link /w/spring-boot-loader/ /w/
COPY --from=extract --link /w/application/ /w/

CMD [ "java", "org.springframework.boot.loader.JarLauncher" ]
HEALTHCHECK CMD wget http://localhost:8080/actuator/health || exit 1
