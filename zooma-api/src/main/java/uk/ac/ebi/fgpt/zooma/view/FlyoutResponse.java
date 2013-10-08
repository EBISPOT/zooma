package uk.ac.ebi.fgpt.zooma.view;

/**
 * A snippet of html that represents a succinct view of a uniquely identified ZOOMA result.
 * <p/>
 * <p/>
 * This object models the type of response expected by the flyout request of the freebase suggest API.  See <a
 * href="http://wiki.freebase.com/wiki/ApiSearch">http://wiki.freebase.com/wiki/ApiSearch</a> for more details.
 *
 * @author Tony Burdett
 * @date 05/04/12
 */
public interface FlyoutResponse {
    /**
     * Returns the identifier of this entity
     *
     * @return an identifier for this entity
     */
    String getId();

    /**
     * Returns an HTML fragment that can be used to render a simple view of this result to show to users
     *
     * @return the html view of this result
     */
    String getHtml();
}
