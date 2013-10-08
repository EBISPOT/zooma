package uk.ac.ebi.fgpt.zooma.access;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.fgpt.zooma.model.Identifiable;
import uk.ac.ebi.fgpt.zooma.util.PropertiesMapAdapter;

import java.net.URI;

/**
 * Acts as an endpoint that offers an implementation of the google and freebase suggest API to offer search and
 * autocomplete functionality over zooma identifiable objects.
 * <p/>
 * For more information on the suggest API, see <a href="http://code.google.com/p/google-refine/wiki/SuggestApi">http://code.google.com/p/google-refine/wiki/SuggestApi</a>.
 * Implementations of this class should return matching results using ZOOMA functionality behind the scenes.
 *
 * @param <T> the type of identifiable objects this acts as an endpoint for
 * @author Tony Burdett
 * @date 30/03/12
 */
public abstract class IdentifiableSuggestEndpoint<T extends Identifiable> extends SuggestEndpoint<T, URI> {
    private PropertiesMapAdapter propertiesMapAdapter;

    public PropertiesMapAdapter getPropertiesMapAdapter() {
        return propertiesMapAdapter;
    }

    @Autowired
    public void setPropertiesMapAdapter(PropertiesMapAdapter propertiesMapAdapter) {
        this.propertiesMapAdapter = propertiesMapAdapter;
    }

    @Override protected String extractElementID(T t) {
        return t.getURI().toString();
    }
}
