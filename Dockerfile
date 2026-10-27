
# Build stage
# Add code for maven build
FROM maven:3.9-eclipse-temurin-11 as build-stage
COPY src /home/app/src
COPY pom.xml /home/app

RUN mvn -f /home/app/pom.xml clean install


FROM eclipse-temurin:11-jre

#Setting the work dir as tmp coz
WORKDIR /tmp

#Copy pbf-data-loader jar from target folder
COPY --from=build-stage /home/app/target/*.jar /tmp/pbf-data-loader.jar


#Start PBF Data Loader
CMD ["java","-jar","pbf-data-loader.jar"]