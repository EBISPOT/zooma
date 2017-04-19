# zooma
ZOOMA - Optimal Ontology Mapping Application. http://www.ebi.ac.uk/spot/zooma.

# Quickstart


  Install Maven and JDK 8
  
  Install docker-compose https://docs.docker.com/compose/

  `mvn package`

  `docker-compose build`

  `docker-compose up`

 public zooma-mongo api: http://localhost:8080
 
 public zooma-solr api: http://localhost:8081
 
 public zooma-neo4j api: http://localhost:8082

 internal Solr endpoint at http://localhost:8983/
 
 internal Neo4j endpoint at http://localhost:7474
 
 internal RabbitMQ endpoint at http://localhost:15672/
 
To stop docker containers from running: 

`docker-compose down`
 
 To load data to the application in the container:
 - Edit the application.properties.example file in zooma-csv-loader
 - Rename it to application.properties
 - Run `mvn package` again
 - Run `java -jar zooma-csv-loader-<version>.jar` from the target directory of zooma-csv-loader while the container is running
 
 # Persist Data
 
 If you want the data that you load to persist, edit the `docker-compose.yml` file and uncomment the lines showed below in solr, mongo and neo4j services respectively, and the data will be saved in the paths specified. 

solr:

`#- ./zooma-solr/src/main/sample_data/data/:/home/mysolrhome/annotation/data/`

mongo:

`#volumes:`

`#- ./zooma-mongo/src/main/sample_data:/data/db`
        
neo4j:

`#volumes:`

` #- ./zooma-neo4j/src/main/sample_data:/data`
 
 If you stop docker and re-run it with the volumes loaded, you need go to edit the `docker-compose.yml` file and from the zooma-neo4j service comment out the 
 
 `- spring.data.neo4j.indexes.auto=assert` line and 
 
 un-comment the `- spring.data.neo4j.indexes.auto=none` line.
