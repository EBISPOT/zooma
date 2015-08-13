/* Zooma2 Javascript library, uses freebase suggest widget for autocomplete functionality.  This file declares a zooma jQuery plugin and performs any required zooma setup on document load.  To use, simply create a script element that binds a  */
var JAVASCRIPT = "js";
var STYLESHEET = "css";

var annotationSummarySuffix = "_annotation_summary";
var propertyTypeSuffix = "_property_type";
var propertyValueSuffix = "_property_value";
var annotatesToSuffix = "_annotates_to";

var annotationSummaryClass = "zooma_annotation_summary";
var propertyTypeClass = "zooma_property_type";
var propertyValueClass = "zooma_property_value";
var annotatesToClass = "zooma_annotates_to";

// Declares a zooma jQuery plugin
(function($) {
    $.fn.zooma = function(options) {
        var $this = $(this);

        // configure the jQuery object this was called on as a zooma plugin
        log("Adding zooma functionality to " + $this.attr("id"));

        //noinspection JSValidateTypes
        var _options = $.extend({
            'api_base_url': 'http://www.ebi.ac.uk/spot/zooma/',
            'api_base_path': 'v2/api'
        }, options);

        // create some defaults and allow options to extend
        //noinspection JSValidateTypes
        var zoomaOptions = $.extend({
            'service_url': _options.api_base_url,
            'flyout_service_url': _options.api_base_url + _options.api_base_path,
            'service_path': _options.api_base_path + '/search',
            prefixed: true,
            filter: undefined,
            zIndex: 10
        }, _options);

        log("ZOOMA settings loaded: initializing with the following configuration...");
        log(JSON.stringify(zoomaOptions));
        try {
            // create a div after the supplied element to store zooma results
            log("Creating placeholder divs to store selected annotation info");
            var id = this.attr("id") + "_zooma_results";
            $this.after('<div id="' + id + '"></div>');

            $("#" + id)
                .append('<div id="' + id + annotationSummarySuffix + '" class="' + annotationSummaryClass + '" />')
                .append('<div id="' + id + propertyTypeSuffix + '" class="' + propertyTypeClass + '" />')
                .append('<div id="' + id + propertyValueSuffix + '" class="' + propertyValueClass + '" />')
                .append('<div id="' + id + annotatesToSuffix + '" class="' + annotatesToClass + '" />')
                .hide();
            log("Binding suggest handling to " + $this);
            $this.suggest(zoomaOptions).bind("fb-select", function(e, data) {
                onSelect(data, zoomaOptions, $this, $("#" + id));
            });
            log("ZOOMA initialization complete!");
            return this;
        }
        catch (ex) {
            log("ZOOMA initialization failed: " + ex);
            throw "ZOOMA initialization failed: " + ex;
        }
    };
})(jQuery);

$(document).ready(function() {
    if ($("#zooma-log").length == 0) {
        // create hidden zooma-log div if it doesn't exist
        $("body").append("<div id=\"zooma-log\" />");
        $("#zooma-log").hide();
    }

    // now setup
    try {
        loadLibraries([
                {location: "https://www.gstatic.com/freebase/suggest/3_1/suggest.min.js", type: JAVASCRIPT},
                {location: "https://www.gstatic.com/freebase/suggest/3_1/suggest.min.css", type: STYLESHEET}
            ],
            function() {
                log("Finished loading all libraries");
            });
    }
    catch (ex) {
        log("Failed to load libraries: " + ex);
    }
});

function onSelect(result, options, input, update) {
    log("ZOOMA result '" + result.id + "' was selected, input element is " + input.attr("id"));
    var ajaxUrl = options.service_url + options.api_base_path + "/summaries/" + result.id;

    log("Attempting to get data from ZOOMA... CORS supported: " + $.support.cors + "; Summary ID: " + result.id + ".");
    try {
        $.getJSON(ajaxUrl, function(data) {
            log("Got ajax response...");
            log("Updating " + update.attr("id") + " with results");
            update.children("." + annotationSummaryClass).html(data.id);
            update.children("." + propertyTypeClass).html(data.annotatedPropertyType);
            update.children("." + propertyValueClass).html(data.annotatedPropertyValue);
            var annotatesTo = "";
            for (var i = 0; i > data.semanticTags.length - 1; i++) {
                annotatesTo = annotatesTo + ",";
            }
            annotatesTo = annotatesTo + data.semanticTags[data.semanticTags.length - 1];
            update.children("." + annotatesToClass).html(annotatesTo);
            if (options.onSelect) {
                log("Invoking onSelect callback...");
                try {
                    options.onSelect(data);
                    log("onSelect callback function successfully called");
                }
                catch (ex) {
                    log("onSelect callback failed: " + ex);
                }
            }
            log("ZOOMA has finished sending results");
            return input.change();
        });
    }
    catch (ex) {
        log("AJAX request for " + result.id + " failed: " + ex);
    }
}

/**
 * A function that takes an array of library objects and loads them in turn, before calling the supplied onSuccess callback
 *
 * @param libs the libraries to load
 * @param onSuccess a callback function to run once all libraries have been loaded
 */
function loadLibraries(libs, onSuccess) {
    log("Trying to load " + libs.length + " libraries...");
    if (libs.length > 1) {
        // create a recursive callback function
        loadLibrary(libs[0].location, libs[0].type, function() {
            loadLibraries(libs.slice(1), onSuccess);
        });
    }
    else {
        // load the only element and callback
        loadLibrary(libs[0].location, libs[0].type, onSuccess);
    }
}

function loadLibrary(location, type, callback) {
    log("Loading " + type + " library from " + location + "...");
    if (type == JAVASCRIPT) {
        log("Attempting load of javascript library...");
        $.getScript(location, callback)
            .done(function(script, textStatus) {
                log("Loaded javascript library from " + location + ": " + textStatus);
            })
            .fail(function(jqxhr, settings, exception) {
                log("Failed to load javascript library from " + location + ": " + exception);
                callback();
            });
    }
    else if (type == STYLESHEET) {
        log("Attempting load of cascading style sheet library...");
        $("head").append("<link>");
        css = $("head").children(":last");
        css.attr({
            rel: "stylesheet",
            type: "text/css",
            href: location
        });
        callback();
    }
    else {
        throw "Unknown library type: " + type + "; Failed to load from " + location;
    }
}

function log(msg) {
    $('#zooma-log').append('<div class=\"logmessage\">' + msg + '</div>');
}
