#  Use alpine base for minimal size
#  Includes OpenJDK 8 (and Maven 3)

FROM openjdk:8-jre-alpine

# COPY zooma-mongo/target/*-SNAPSHOT.jar \
#      zooma-solr/target/*-SNAPSHOT.jar \
#      zooma-neo4j/target/*-SNAPSHOT.jar \
#      zooma-predictor/target/*-SNAPSHOT.jar \
#      /home/

#  Shorthand for above: uncomment to test docker build on development machine
COPY zooma-[nmsp][oer]*[^e]/target/*-SNAPSHOT.jar /home/

#  Uncomment for docker build on deployment machine
# COPY *-SNAPSHOT.jar /home/

#  Other required files for zooma image
COPY start_zooma_neo4j.sh /home/
