# Use an official Maven image to build the project
FROM maven:3.9.6-eclipse-temurin-21 as build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and download dependencies first for faster builds
COPY pom.xml .

# Copy and run the download script to get the LookseeCore JAR
COPY scripts/download-core.sh ./scripts/download-core.sh
RUN chmod +x ./scripts/download-core.sh
RUN bash ./scripts/download-core.sh

# Copy the rest of the project source code
COPY src ./src

# Build the application
RUN mvn clean install -DskipTests

# Use a smaller JDK image to run the app
FROM adoptopenjdk/openjdk14
#FROM eclipse-temurin:17-jre

# Copy the built JAR file from the previous stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 443
EXPOSE 80
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Xms256M", "-ea","-jar", "app.jar"]
