
# Build stage
# Add code for maven build
FROM maven:3.8.3-openjdk-11-slim as build-stage
COPY src /home/app/src
COPY pom.xml /home/app

RUN mvn -f /home/app/pom.xml clean install


FROM adoptopenjdk:11-jre-hotspot

#Setting env variable. This is used for location of external properties
ENV PBF-DATA-LOADER_HOME=/tmp

#Setting the work dir as tmp coz
WORKDIR /tmp

#Copy hns-esb jar from target folder
COPY --from=build-stage /home/app/target/*.jar /tmp/pbf-data-loader.jar


#Start HNI-ESB
CMD ["java","-jar","pbf-data-loader.jar"]