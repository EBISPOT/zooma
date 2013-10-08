package uk.ac.ebi.fgpt.zooma.web;

import java.util.ArrayList;

/**
 * A simple request object, designed for serialization, that contains enough information to generate a series of typed
 * or untyped properties
 *
 * @author Tony Burdett
 * @date 03/04/13
 */
public class ZoomaMappingRequest extends ArrayList<ZoomaMappingRequestItem> {
    private static final long serialVersionUID = 8368783084046738815L;
}
