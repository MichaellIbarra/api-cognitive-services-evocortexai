FROM openjdk:17-jdk-alpine AS builder
WORKDIR /app
COPY ./mvnw ./
COPY ./pom.xml  ./
COPY ./.mvn ./.mvn
RUN chmod +x mvnw
RUN ./mvnw clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip && rm -r ./target/
COPY ./src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/api-reactive-0.0.1-SNAPSHOT.jar .
EXPOSE 8001
ENTRYPOINT ["java", "-jar", "api-reactive-0.0.1-SNAPSHOT.jar"]