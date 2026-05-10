FROM maven:3.9.6-eclipse-temurin-21
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

EXPOSE 8070

CMD ["java", "-jar", "target/auth-service-0.0.1-SNAPSHOT.jar"]
