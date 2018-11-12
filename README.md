# Zooma

[rem]: # (This is a comment; ignored by renderer)

ZOOMA — Optimal Ontology Mapping Application: <http://www.ebi.ac.uk/spot/zooma>

## Quickstart

Install Maven and JDK 8. Then run:
  
[rem]: # (Install docker-compose: <https://docs.docker.com/compose/>)

```bash
mvn package
```

Make sure you edit `Dockerfile` to enable the correct `COPY` command: this
depends on which host you are running it from; details are documented in
`Dockerfile` itself. Then build docker images for Zooma, customised Neo4j and
customised Solr (the docker-compose utility is not required for any of these
steps):

[rem]: # (`docker-compose build`)

```bash
docker build -f NeoDockerfile -t neo-preconf4zooma .
docker build -f SolrDockerfile -t solr-preconf4zooma .
docker build -t zooma .
```

Initialise docker swarm (necessary only if this is the inaugural run on the
current host):

```bash
docker swarm init
```

You must ensure that you have allocated sufficient memory for docker to cover
each continainer's (or service's) requirements. If Neo4j wants up to 3 GB, for
example, it might be prudent to give docker itself access to 6 GB, depending on
the other containerised applications in the stack. This can be configured within
docker settings. Be aware that, confusingly, containers may crash without proper
logging in the absence of sufficient memory to run them. Not over-throttling
memory will also make for better stack performance, as it alleviates excessive
swapping and paging.

Assuming that sufficient memory has been allocated, bring up docker services:

[rem]: # (`docker-compose up`)

```bash
docker stack deploy --compose-file docker-compose.yml zooma
```

public zooma-mongo api: <http://localhost:8080>
 
public zooma-solr api: <http://localhost:8081>
 
public zooma-neo4j api: <http://localhost:8082>
 
public zooma-predictor api: <http://localhost:8083>

internal Solr endpoint at <http://localhost:8983>

internal Neo4j endpoint at <http://localhost:7474>
 
internal RabbitMQ endpoint at <http://localhost:15672>
 
To stop docker containers from running: 

[rem]: # (`docker-compose down`)

```bash
docker stack rm zooma
```
 
To load data to the application in the container:

- Edit the application.properties.example file in zooma-csv-loader
- Rename it to application.properties
- Run `mvn package` again
- Run `java -jar zooma-csv-loader-<version>.jar` from the target directory of
  zooma-csv-loader while the container is running

## Persist Data

If you want the data that you load to persist, edit `docker-compose.yml` and
uncomment the lines shown below in the solr, mongo and neo4j services
respectively, and the data will be saved in the volumes specified.

solr:

`#volumes:`

`#- solr_data:/home/mysolrhome/annotation/data`

mongo:

`#volumes:`

`#- mongo_data:/data/db`

neo4j:

`#volumes:`

`#- neo4j_data:/data`
 
If you stop docker and re-run it with the volumes loaded, you need go to edit
the `docker-compose.yml` file and from the zooma-neo4j service comment out the
 
`- spring.data.neo4j.indexes.auto=assert` line ... 
 
... and un-comment the `- spring.data.neo4j.indexes.auto=none` line.
 
## Search zooma
 
Default behaviour — Zooma will first look into the datasources and _if_ nothing
is found it will look into all the ontologies in the Ontology Lookup Service
(<https://www.ebi.ac.uk/ols>):
 
- by property value:
  <http://localhost:8083/predictions/annotate?q=propertyValue>
 
- by property type _and_ property value:
  <http://localhost:8083/predictions/annotate?type=propertyType&q=propertyValue>
 
## Other options

To further customise the above searches:

- to boost specific topics or sources, add `&origins=source1,source2,topic1`

- to boost specific ontologies, add `&ontologies=ontology1,ontology2`

- to filter (i.e. return results from only) specific topics, sources, or
  ontologies, also add `&filter=true` when adding the `origins` and/or
  `ontologies` fields

- to omit searching in the datasources, add `&origins=none`

- to omit searching in OLS, add `&ontologies=none`

- to include results from OLS even if we get results back from the datasources,
  add `&onto_as_equals=true`

The above can be combined.
