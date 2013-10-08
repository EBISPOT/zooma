package uk.ac.ebi.fgpt.zooma.view;

/**
 * A lightweight representation of the results suggested by any ZOOMA query.
 * <p/>
 * This response contains a list of results that may match the query, in order of relevance wherever possible.  This is
 * a very simple arrangement of strings that represents the actual result in a very generic way as a means of providing
 * rapid, easy to parse results to a query.  Results should contain enough information to allow a client to retrieve the
 * full record if required.
 * <p/>
 * This object models the type of response expected by the freebase suggest API.  See <a
 * href="http://wiki.freebase.com/wiki/ApiSearch">http://wiki.freebase.com/wiki/ApiSearch</a> for more details.
 *
 * @author Tony Burdett
 * @date 04/04/12
 */
public interface SearchResponse {
    /**
     * A string that corresponds to the HTTP response code that would be issued for requests made via a REST APU.
     *
     * @return the HTTP response code representing this response
     */
    String getStatus();

    /**
     * Returns a list of {@link Result}s that make up this response.
     *
     * @return the results that match the query.
     */
    Result[] getResult();

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
        String getMid();

        /**
         * Returns the name of this entity.
         *
         * @return the entity name
         */
        String getName();

        /**
         * Returns the score of this result, out of 100
         *
         * @return the score of this result
         */
        String getScore();

        /**
         * Returns the most notable type of this entity
         *
         * @return the most notable type of this entity
         */
        Notable getNotable();

        /**
         * A simple representation describing the most notable type of any entity.  This allows disambiguation of
         * similarly named entities.
         */
        public interface Notable {
            /**
             * Returns the identifier of of the notable entity.
             *
             * @return an identifier for this type
             */
            String getId();

            /**
             * Returns zooma's assessment of the best single disambiguating description for this entity. It's useful
             * when offering users a list of entities in a UI to pick from if the entities have the same/similar names
             *
             * @return the type name
             */
            String getName();
        }
    }
}
