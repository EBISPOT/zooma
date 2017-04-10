package uk.ac.ebi.spot.zooma.messaging.solr;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/01/17
 */
public class Constants {
    public class Queues {
        public static final String ANNOTATION_SAVE_SOLR = "annotation.save.solr.queue";
        public static final String ANNOTATION_REPLACE_SOLR = "annotation.replace.solr.queue";
    }

    public class Exchanges {
        public static final String ANNOTATION_FANOUT = "annotation.fanout.exchange";
        public static final String ANNOTATION_FANOUT_REPLACEMENT = "annotation.fanout.replace.exchange";
    }
}
