package uk.ac.ebi.spot.zooma.utils.predictor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Created by olgavrou on 17/05/2017.
 */
public class URIUtils {

    private static Logger log = LoggerFactory.getLogger(URIUtils.class);

    protected static Logger getLog() {
        return log;
    }

    /**
     * Returns the sort-of fragment of the supplied URI, stripping it down to just the URI fragment (if present) or else
     * the final part of the path.  This is to extract the commonly used identifier names of a URI for concepts within
     * an ontology.  The most commonly used schemes are "http://my.ontology.org/ontology/name" or
     * "http://my.ontology.org/ontology#name", but this method also captures the older naming convention used in CHEBI
     * (and similar) where the name is of the form "http://my.ontology.org/ontology/search?id=name"
     *
     * @param uri the URI to shorten
     * @return the fragment name of the supplied concept
     */
    public static String extractFragment(URI uri) {
        // convert uri to string
        getLog().trace("Attempting to extract fragment name of URI '" + uri + "'");
        String termURI = uri.toString();

        String fragmentName;

        // chebi uris, everything after last '='
        if (termURI.contains("http://www.ebi.ac.uk/chebi/searchId")) {
            getLog().trace("Extracting fragment name using CHEBI rule");
            fragmentName = termURI.substring(termURI.lastIndexOf("=") + 1);
        }
        else {
            // if it's something other than chebi, we want the real final part of the URI...
            if (uri.getFragment() != null) {
                // a uri with a non-null fragment, so use this...
                getLog().trace("Extracting fragment name using URI fragment (" + uri.getFragment() + ")");
                fragmentName = uri.getFragment();
            }
            else if (uri.getPath() != null) {
                // no fragment, but there is a path so try and extract the final part...
                if (uri.getPath().contains("/")) {
                    getLog().trace("Extracting fragment name using final part of the path of the URI");
                    fragmentName = uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
                }
                else {
                    // no final path part, so just return whole path
                    getLog().trace("Extracting fragment name using the path of the URI");
                    fragmentName = uri.getPath();
                }
            }
            else {
                // not a chebi id, no fragment, path is null, we've run out of rules so don't shorten
                getLog().trace("No rules to shorten this URI could be found (" + termURI + ")");
                fragmentName = termURI;
            }
        }

        getLog().trace("URI: " + uri + " -> fragment name: " + fragmentName);
        return fragmentName;
    }
}
