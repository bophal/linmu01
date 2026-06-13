echo FROM maven:3.9-eclipse-temurin-17-alpine AS build > Dockerfile
echo WORKDIR /app >> Dockerfile
echo COPY pom.xml . >> Dockerfile
echo RUN mvn dependency:go-offline -B >> Dockerfile
echo COPY src ./src >> Dockerfile
echo RUN mvn clean package -DskipTests >> Dockerfile
echo. >> Dockerfile
echo FROM eclipse-temurin:17-jre-alpine >> Dockerfile
echo WORKDIR /app >> Dockerfile
echo COPY --from=build /app/target/*.jar app.jar >> Dockerfile
echo EXPOSE 8081 >> Dockerfile
echo ENTRYPOINT ["java", "-jar", "app.jar"] >> Dockerfile