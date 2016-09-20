package uk.ac.ebi.spot.config;

import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.spot.model.AnnotationSource;
import uk.ac.ebi.spot.model.MongoAnnotationSource;
import uk.ac.ebi.spot.model.MongoDatabaseAnnotationSource;
import uk.ac.ebi.spot.model.MongoOntologyAnnotationSource;
import org.json.JSONObject;


import java.io.UnsupportedEncodingException;
import java.net.URI;

/**
 * Created by olgavrou on 12/09/2016.
 */
public class String2MongoSourceConverter implements Converter<String, AnnotationSource> {

    @Override
    public AnnotationSource convert(String source) {
        URI uri;
        AnnotationSource.Type type = null;
        String name;

        try{
            String sourceDecoded = java.net.URLDecoder.decode(source, "UTF-8");
            JSONObject jsonObject = new JSONObject(sourceDecoded);

            uri = URI.create(jsonObject.getString("uri"));
            String typeS = jsonObject.getString("type");
            if (typeS.equals("DATABASE")){
                type = AnnotationSource.Type.DATABASE;
            } else if (typeS.equals("ONTOLOGY")){
                type = AnnotationSource.Type.ONTOLOGY;
            }
            name = jsonObject.getString("name");
            System.out.println();

        } catch (UnsupportedEncodingException e){
            //return empty source
            return new MongoAnnotationSource(URI.create(""), "", null);
        }

        if (type == AnnotationSource.Type.ONTOLOGY){
            return new MongoOntologyAnnotationSource(uri, name, "", "");
        } else if (type == AnnotationSource.Type.DATABASE){
            return new MongoDatabaseAnnotationSource(uri, name);
        }
        return new MongoAnnotationSource(URI.create(""), "", null);
    }
}
