package uk.ac.ebi.fgpt.zooma.search;

/**
 * Created with IntelliJ IDEA.
 * User: jmcmurry
 * Date: 23/10/2013
 * Time: 17:45
 * To change this template use File | Settings | File Templates.
 */
public class Junk {
    public static void main(String[] args) {
        String uri = "http://purl.org/obo/owl/NCBITaxon#NCBITaxon_10090";
        int delimiterIndex = Math.max(uri.lastIndexOf("/"), uri.lastIndexOf("#")) + 1;
        String namespace =  uri.substring(0, delimiterIndex - 1);
        String olsShortId = uri.substring(delimiterIndex).replace("_",":");
        System.out.println(namespace);
        System.out.println(olsShortId);
    }
}
