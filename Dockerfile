#  Use alpine base for minimal size
#  Includes OpenJDK 8 (and Maven 3)

FROM openjdk:8-jre-alpine

#  On the command line, set any string value for DEV, e.g. "yes" if building in
#  the development (as opposed to deployment) environment; otherwise leave unset

ARG dev
ENV mongodir ./${dev:+zooma-mongo/target/}
ENV solrdir ./${dev:+zooma-solr/target/}
ENV neo4jdir ./${dev:+zooma-neo4j/target/}
ENV predictordir ./${dev:+zooma-predictor/target/}

#  Copy jars

COPY ${mongodir}/zooma-mongo-3.0.0-SNAPSHOT.jar /home/
COPY ${solrdir}/zooma-solr-3.0.0-SNAPSHOT.jar /home/
COPY ${neo4jdir}/zooma-neo4j-3.0.0-SNAPSHOT.jar /home/
COPY ${predictordir}/zooma-predictor-3.0.0-SNAPSHOT.jar /home/

#  Other required files for zooma image

COPY start_zooma_neo4j.sh /home/
