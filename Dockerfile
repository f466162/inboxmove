FROM maven:3-eclipse-temurin-17 as build

COPY src /build/src
COPY pom.xml /build/
WORKDIR /build

RUN mvn -T1C -B clean package

FROM eclipse-temurin:17

COPY --from=build /build/target/inboxmove-*.jar /app.jar

USER nobody

CMD ["java", "-jar", "/app.jar"]
