FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /opt/app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# RUN ./mvnw dependency:go-offline
COPY ./src ./src
RUN ./mvnw clean package -Dmaven.test.skip=true


FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/app

# Run as non-root user
#RUN addgroup -S --gid 10001 spring && adduser -S --uid 10001 spring -G spring
#USER spring:spring

#RUN apt-get update && apt-get install -y ffmpeg
HEALTHCHECK --interval=10s --timeout=3s --retries=3 CMD curl http://localhost:8888/auth/test || exit 1
COPY --from=builder /opt/app/target/*.jar /opt/app/app.jar

# Currently using jasypt to encrypt application.properties,TODO use k8s config instead of copy properties to docker image
#COPY ./src/main/resources/* /opt/app/properties/
#ENTRYPOINT ["java", "-jar", "/opt/app/app.jar", "--spring.config.location=/opt/app/properties/", "--spring.profiles.active=test"]

ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
