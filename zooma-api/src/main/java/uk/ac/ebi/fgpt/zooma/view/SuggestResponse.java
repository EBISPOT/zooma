package uk.ac.ebi.fgpt.zooma.view;

import java.io.Serializable;

/**
 * A lightweight representation of the results suggested by any ZOOMA query.
 * <p/>
 * This response contains a list of results that may match the query, in order of relevance wherever possible.  This is
 * a very simple arrangement of strings that represents the actual result in a very generic way as a means of providing
 * rapid, easy to parse results to a query.  Results should contain enough information to allow a client to retrieve the
 * full record if required.
 * <p/>
 * This object models the type of response expected by the google refine suggest API.  See <a
 * href="http://code.google.com/p/google-refine/wiki/SuggestApi">http://code.google.com/p/google-refine/wiki/SuggestApi</a>
 * for more details.
 *
 * @author Tony Burdett
 * @date 29/03/12
 */
public interface SuggestResponse extends Serializable {
    /**
     * Returns error or success state of the API that generated this response, at an abstract level above the level of
     * HTTP. For example, use "/api/status/error" if there's an error.
     *
     * @return the state of the API that generated this response
     */
    String getCode();

    /**
     * A string that corresponds to the HTTP response code that would be issued for requests made via a REST APU.
     *
     * @return the HTTP response code representing this response
     */
    String getStatus();

    /**
     * Returns the prefix parameter that was supplied in the request, echoed back.
     *
     * @return the prefix parameter that formed part of the query
     */
    String getPrefix();

    /**
     * Returns a list of {@link Result}s that make up this response.
     *
     * @return the results that match the query.
     */
    Result[] getResults();

    /**
     * A simple representation of a result that can be used to answer a query to a suggest API.  This result is an
     * extremely lightweight representation of any entity that can be precisely identified by an ID with an identifier
     * space.
     */
    public interface Result {
        /**
         * Returns the identifier of this entity
         *
         * @return an identifier for this entity
         */
        String getId();

        /**
         * Returns the name of this entity.
         *
         * @return the entity name
         */
        String getName();

        /**
         * Returns the most notable type of this entity
         *
         * @return the most notable type of this entity
         */
        NType getNType();

        /**
         * A simple representation describing the most notable type of any entity.  This allows disambiguation of
         * similarly named entities.
         */
        public interface NType {
            /**
             * Returns the identifier of this type
             *
             * @return an identifier for this type
             */
            String getId();

            /**
             * Returns the name of this type
             *
             * @return the type name
             */
            String getName();
        }
    }
}
