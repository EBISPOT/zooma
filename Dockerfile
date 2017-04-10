#use alpine base for minimal size
#includes OpenJDK 8 (and Maven 3)
FROM openjdk:8-jre-alpine

COPY zooma-mongo/target/*.jar zooma-solr/target/*.jar /home/ 
