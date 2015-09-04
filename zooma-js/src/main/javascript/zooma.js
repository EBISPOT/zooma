/* Zooma2 Javascript client library.  This plugin enables ZOOMA autocomplete and annotation functions to be bound to appropriate elements. */

// defaults
var default_zooma_url = 'http://www.ebi.ac.uk/spot/zooma/';
var default_zooma_base_path = 'v2/api/services';
var default_logging_div = 'zooma-log';

// Declares a zooma jQuery plugin
(function($) {
    var logging; // should be one of 'console', 'div', or 'none'
    var loggingDiv; // defined if logging == 'div'

    var commands = {
        autocomplete: function(target, options) {
            var settings = extendOptions(options);
            initLogging(settings);
            return autocomplete(target, settings);
        },
        annotate: function(target, options) {
            var settings = extendOptions(options);
            initLogging(settings);


        }
    };

    var extendOptions = function(options) {
        // create some defaults and allow options to extend
        var _options = $.extend({
            'zooma_url': default_zooma_url,
            'zooma_base_path': default_zooma_base_path
        }, options);

        return $.extend({
            'zooma_suggest_path': _options.zooma_url + _options.zooma_base_path + '/suggest',
            'zooma_annotate_path': _options.zooma_url + _options.zooma_base_path + '/annotate',
            'logging': 'none',
            'loggingDiv': default_logging_div
        }, _options);
    };

    var initLogging = function(settings) {
        // setup logging
        if (settings.logging == 'console' || settings.logging == 'div' || settings.logging == 'none') {
            logging = settings.logging;
            if (logging == 'div') {
                loggingDiv = $("#" + settings.loggingDiv);
            }
        }
        else {
            throw "logging option must be one of 'console', 'div' or 'none'";
        }
    };

    var log = function(msg) {
        if (logging == 'console') {
            console.log(msg);
        }
        if (logging == 'div') {
            if (loggingDiv.length == 0) {
                // create hidden zooma-log div if it doesn't exist
                $("body").append("<div id=\"zooma-log\" />");
                $("#zooma-log").hide();
            }
            loggingDiv.append('<div class=\"logmessage\">' + msg + '</div>');
        }
    };

    var autocomplete = function(target, settings) {
        var $this = $(target);

        // log setup
        log("Adding zooma autocomplete functionality to " + $this.attr("id"));
        log("ZOOMA settings loaded: initializing with the following configuration...");
        log(JSON.stringify(settings));

        // add autocomplete functionality to the selected element
        $this.autocomplete({
            source: function(request, response) {
                $.getJSON(settings.zooma_suggest_path, {prefix: request.term}, response);
            },
            minLength: 3
        });
    };

    var annotate = function() {

    };

    $.fn.zooma = function(command, options) {
        if (commands[command]) {
            return this.each(function() {
                return commands[command].apply(this, [this, options]);
            });
        }
        else {
            $.error("jQuery.zooma plugin does not support the command '" + command + "'");
        }
    };
})(jQuery);