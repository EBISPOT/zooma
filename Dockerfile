#  Use alpine base for minimal size
#  Includes OpenJDK 8 (and Maven 3)

FROM openjdk:8-jre-alpine

# COPY zooma-mongo/target/*-SNAPSHOT.jar \
#      zooma-solr/target/*-SNAPSHOT.jar \
#      zooma-neo4j/target/*-SNAPSHOT.jar \
#      zooma-predictor/target/*-SNAPSHOT.jar \
#      /home/

#  Shorthand for above: uncomment to test docker build on development machine
# COPY zooma-[msnp][oer]*[^e]/target/*-SNAPSHOT.jar /home/
COPY zooma-*/target/zooma-[msnp]*-SNAPSHOT.jar /home/

#  Uncomment for docker build on deployment machine
# COPY zooma-[msnp]*-SNAPSHOT.jar /home/

#  Other required files for zooma image
COPY start_zooma_neo4j.sh /home/
