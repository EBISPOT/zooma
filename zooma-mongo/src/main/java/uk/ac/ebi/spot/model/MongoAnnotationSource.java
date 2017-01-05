package uk.ac.ebi.spot.model;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by olgavrou on 04/08/2016.
 */
@Document(collection = "datasources")
public class MongoAnnotationSource extends MongoDocument implements AnnotationSource {

    private String uri;
    private Type type;
    private String name;
    private String topic;


    public MongoAnnotationSource(String uri, String name, Type type, String topic) {
        this.uri = uri;
        this.name = name;
        this.type = type;
        this.topic = topic;
    }


    @Override public Type getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getUri() { return uri; }

    @Override
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MongoAnnotationSource{" +
                "uri=" + uri +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", topic='" + topic + '\'' +
                '}';
    }
}
