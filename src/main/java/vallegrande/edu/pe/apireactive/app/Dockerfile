FROM openjdk:17-jdk-alpine AS builder
WORKDIR /app

COPY ./mvnw ./

COPY ./pom.xml  ./

COPY ./.mvn ./.mvn

RUN chmod +x ./mvnw

RUN ./mvnw clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip && rm -r ./target/

COPY ./src ./src

RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-alpine

WORKDIR /app

RUN addgroup -g 1028 devopsc && \
    adduser -D -G devopsc admin

VOLUME /tmp
COPY --from=builder /app/target/api-reactive-0.0.1-SNAPSHOT.jar /app/app.jar

RUN chown -R admin:devopsc /app

USER admin

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]