/* Zooma2 Javascript client library.  This plugin enables ZOOMA autocomplete and annotation functions to be bound to appropriate elements. */

// defaults
var default_zooma_url       = 'http://www.ebi.ac.uk/spot/zooma/';
var default_zooma_base_path = 'v2/api/services';
var default_logging_div     = 'zooma-log';
var default_ols_search_end  = 'http://www.ebi.ac.uk/ols/beta/api/terms?short_form=';
var default_tooltip_template = '<div class=\"zooma-popup\">\n\t<header class=\"popup-header\">\n\t\t<h3>{{term}}</h3>\n\t\t{{#isUserTerm}}\n\t\t\t<h5>Term provided by <span class=\"confidence-label {{#lower}}{{confidence}}{{/lower}}-confidence-label\">{{confidence}}</span></h5>\n\t\t{{/isUserTerm}}\n\t\t{{^isUserTerm}}\n\t\t\t<h5>Term suggested by ZOOMA with <span class=\"confidence-label {{#lower}}{{confidence}}{{/lower}}-confidence-label\">{{confidence}}</span> confidence</h5>\n\t\t{{/isUserTerm}}\n\t</header>\n\t<section class=\"popup-body\">\n\t\t<h3>Description:</h3>\n\t\t{{#excerpt}}\n\t\t\t{{#description}}\n\t\t\t\t<p>{{.}}</p>\n\t\t\t{{/description}}\n\t\t{{/excerpt}}\n\t</section>\n\t<footer class=\"popup-footer\">\n\t\t<a href=\"{{{iri}}}\" target=\"_blank\">{{{iri}}}</a>\t\n\t</footer>\n</div>';
//var data_source_required_format = 'required:[cmpo,efo,ebisc]'
//var data_source_preferred_format = 'preferred:[ebisc]'




// Declares a zooma jQuery plugin
(function($,Mustache) {
    var logging; // should be one of 'console', 'div', or 'none'
    var loggingDiv; // defined if logging == 'div'
    var popupTemplate; // shared popup template


    var commands = {
        initOptions: function(target,options) {
            var settings = extendOptions(options);
            initLogging(settings);
            initTooltip(settings);
            return settings;
        },

        autocomplete: function(target, options) {
            var settings = commands.initOptions(target,options);
            return autocomplete(target, settings);
        },

        annotate: function(target, options) {
            var settings = commands.initOptions(target,options);
            return annotate(target, settings);
        }
    };

    var extendOptions = function(options) {
        // create some defaults and allow options to extend
        var _options = $.extend({
            'zooma_url': default_zooma_url,
            'zooma_base_path': default_zooma_base_path,
			'zooma_datasources': [],
			'zooma_preferred_datasources': [],
            'zooma_property_type': 'disease'
        }, options);

        return $.extend({
            'zooma_suggest_path': _options.zooma_url + _options.zooma_base_path + '/suggest',
            'zooma_annotate_path': _options.zooma_url + _options.zooma_base_path + '/annotate',
            'logging': 'none',
            'loggingDiv': default_logging_div,
            'tooltipOrigin': 'default'
        }, _options);
    };

    var createSpinner = function() {
        return $('<span>').attr('class','zooma-spinner').append($('<div>').addClass('throbber-loader'));
    };

    var createCustomTermButton = function() {
        return $("<button class='btn-custom-term'>Use custom term</button>");   
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

    var initTooltip = function(settings) {
        if (settings.tooltipOrigin == 'default') {
            popupTemplate = default_tooltip_template;
        } else {
            try {
                $.get(settings.tooltipOrigin, function(content) {
                    popupTemplate = content;
                });
            } catch (e) {
                log('Something went wrong!\nUnable to use ' + settings.tooltipOrigin + ' as custom template.\nRelying on the default template');
                popupTemplate = default_tooltip_template;
            }
        }
        Mustache.parse(popupTemplate);
    };

    var log = function(msg) {
        if (logging == 'console') {
            try {
                console.log(msg);
            }
            catch (ex) {
                console.log("Failed to log (" + ex + ")");
            }
        }
        if (logging == 'div') {
            try {
                if (loggingDiv.length === 0) {
                    // create hidden zooma-log div if it doesn't exist
                    $("body").append("<div id=\"zooma-log\" />");
                    $("#zooma-log").hide();
                }
                loggingDiv.append('<div class=\"logmessage\">' + msg + '</div>');
            }
            catch (ex) {
                loggingDiv.append("Failed to log (" + ex + ")");
            }

        }
    };

    var isType = function(varToCheck,typeToCheck) {
        typeToCheck = typeToCheck.slice(0,1).toUpperCase() + typeToCheck.slice(1).toLowerCase();
        switch(typeToCheck) {
            case 'Array':
            case 'Function':
            case 'Object':
            case 'String':
            case 'Number':
            case 'Date':
                break;
            default:
                return false;
        }
        var getType = {};
        var objType = '[object ' + typeToCheck + ']';
        return varToCheck && getType.toString.call(varToCheck) === objType;
    };

    var isFunction = function(varToCheck) {
        return isType(varToCheck,'Function');
    };

    var isObject = function(varToCheck) {
        return isType(varToCheck,'Object');
    };

    var isArray = function(varToCheck) {
        return isType(varToCheck,'Array');
    };

    var isString = function(varToCheck) {
        return isType(varToCheck,'String');
    };

    var isSelector = function(expressionToCheck) {
        try {
            var $element = $(expressionToCheck);
            log(expressionToCheck + " is a valid jQuery selector (selects " + $element + ")");
            return true;
        }
        catch (error) {
            log(error);
            return false;
        }
    };

    var pressEnter = function(target, callback) {
        return target.bind('keypress', function(e) {
            if (e.keyCode == 13) {
                callback.apply(this, [e]);
            }
        });
    };

    var pressEscape = function(target, callback) {
        return target.bind('keyup', function(e) {
            if (e.keyCode == 27) {
                callback.apply(this, [e]);
            }
        });
    };

    var excerpt = function(value, maxLength) {

        var truncValue = value;

        function trunc(value,maxLength) {
            if (value.length > maxLength) {
                return value.slice(0,maxLength) + "...";
            }
            return value;   
        }

        if (typeof value !== "undefined" && value !== null) {
            if (typeof maxLength === "undefined" || maxLength === null) {
                maxLength = 300;
            }
            
            if (isArray(value)) {
                for (var i=0; i<value.length;i++) {
                    truncValue.push(trunc(value[i],maxLength));
                }
            } else if (isString(value)) {
                truncValue = trunc(value,maxLength);
            }
        }
        return truncValue;
    };

    // var getTermCatalog = function(term) {
    //     var matches = term.match(/([A-Z]+)_\d+/);
    //     if (matches && matches.length > 1) {
    //         return matches[1].toLowerCase();    
    //     } 
    //     return "efo";
    // };

    var setupTooltip = function(target,zoomaInfo){

        // var catalog = getTermCatalog(zoomaInfo.shortname);
        // var olsEndPoint = default_ols_url + catalog + "/" + default_ols_search_end + zoomaInfo.shortname;
        var olsEndPoint = default_ols_search_end + zoomaInfo.shortname;

        target.tooltipster({
            content: "Loading...",
            contentAsHTML: true,
            interactive: true,
            maxWidth: 400,
            trigger: 'hover',
            theme: 'tooltipster-light',
            functionBefore: function(origin,continueTooltip) {

                continueTooltip();

                if (origin.data('ajax') !== 'cached') {
                    $.ajax({
                        type: 'GET',
                        url: olsEndPoint,
                        success: function(data) {
                            var term = data._embedded.terms[0];
                            var tooltipData = {};

                            tooltipData.term        = term.label;
                            tooltipData.confidence  = zoomaInfo.confidence;
                            tooltipData.description = term.description;
                            tooltipData.iri         = term.iri;
                            tooltipData.isUserTerm  = zoomaInfo.confidence === "USER";
                            tooltipData.lower       = function() {
                                return function (text, render) {
                                    return render(text).toLowerCase();
                                };
                            };
                            tooltipData.excerpt     = function() {
                                return function(text,render) {
                                    return excerpt(render(text),400) + "</p>";
                                };
                            };

                            var renderedTemplate = Mustache.render(popupTemplate,tooltipData);
                            origin.tooltipster('content',renderedTemplate).data('ajax','cached');
                        },

                        error: function(data) {
                            if (zoomaInfo.confidence === 'USER') {
                                origin.tooltipster('content','No informations for this custom term').data('ajax','cached');
                            } else {
                                origin.tooltipster('content','Error retrieving content').data('ajax','cached');    
                            }
                            
                        }
                    });
                }

            }
        });
        // target.hintModal();
        // target.append("<div class=\"hintModal_container\">" + target.attr('id') + "</div>");
    };
    
    var retrieveZoomaAnnotations = function(settings, target, property) {
        log("Retrieving zooma suggested terms for " + property);
        target.siblings('.zooma-annotations-container').show();
        target.tagsinput('removeAll');
        target.siblings().find('.zooma-spinner').show();
        target.find('.zooma-spinner').show();
        jQuery.getJSON(prepareZoomaAnnotationRequest(settings,property), function(data) {
            renderZoomaAnnotations(target, data);
        });
    };

	var prepareZoomaAnnotationRequest = function(settings,property){
		var requestURL = settings.zooma_annotate_path + "?propertyValue=" + encodeURI(property);
        
        // Add Property Type
        if ( settings.zooma_property_type ) {
            requestURL += "&propertyType=" + encodeURI(settings.zooma_property_type);
        }

		// Add datasources
		if (settings.zooma_datasources && settings.zooma_datasources.length > 0) {

			requestURL += "&filter=required:[";
			settings.zooma_datasources.forEach(function(element) {
				requestURL = requestURL + element + ',';
			});
            requestURL = requestURL.replace(/,\s*$/, "");
			requestURL += "]";
		

            // Add preferred sources
            if (settings.zooma_preferred_datasources) {
                
                // Check if preferred datasources ar also required
                var valid_preferred = [];
                settings.zooma_preferred_datasources.forEach(function(element,index) {
                    if (settings.zooma_datasources.indexOf(element) > -1) {
                        valid_preferred.push(element);
                    }    
                });

                if (valid_preferred) {
                    requestURL += ";preferred:[";
                    valid_preferred.forEach(function(element) {
                        requestURL += element + ',';	
                    });
                    

                    requestURL = requestURL.replace(/,\s*$/, "");
                    requestURL += "]";
                }
            }
        }
		return requestURL;
	};

    var renderZoomaAnnotations = function(target, annotations) {
        target.siblings().find('.zooma-spinner').hide();
        target.tagsinput('removeAll');
        $(annotations).each(function(index, element) {
            var shortnames = [];
            $(element.semanticTags).each(function(_index, _element) {
                var _parts = _element.split('/');
                var parts = _parts[_parts.length - 1].split('#');
                var shortname = parts[parts.length - 1];
                shortnames.push(shortname);
            });

            if (shortnames.length > 1) {
                var shortname = "";
                for (var i = 0; i < shortnames.length - 1; i++) {
                    shortname = shortname + shortnames[i] + " & ";
                }
                shortname = shortname + shortnames[length - 1];
                element.shortname = shortname;
            }
            else {
                if (shortnames.length > 0) {
                    element.shortname = shortnames[0];
                }
                else {
                    element.shortname = "Unknown";
                }
            }
            target.tagsinput('add', element);
        });
    };

    var autocomplete = function(target, settings) {
        log("Adding zooma autocomplete functionality to " + target.attr("id") + " with configuration: " +
            JSON.stringify(settings));
        var properties = new Bloodhound({
            datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            remote: {
                url: settings.zooma_suggest_path + '?prefix=%QUERY',
                wildcard: '%QUERY'
            }
        });

        log("Binding typeahead to " + target.attr("id") + "...");
        target.typeahead({
                    hint: true,
                    highlight: true,
                    minLength: 3
                },
                {
                    source: properties
                });
        log("Typeahead bound ok!");

        // styling
        var container = $('<div class="zooma-autocomplete-container"></div>');
        target.parent().wrap(container);
        return container;
    };

    var annotate = function(target, settings) {
        log("Adding zooma annotation functionality to " + target.attr("id") + " with configuration: " +
            JSON.stringify(settings));

        // add data-role tagsinput to target and init
        target.attr('data-role', 'tagsinput');
        target.tagsinput({
            trimValue: true, 
            itemValue: 'shortname', 
            itemText: 'shortname',
            freeInput: true,
            confirmKeys: [],
            tagClass: function(item) {

                var classes = ['tooltip','label'];

                switch (item.confidence) {
                    case 'HIGH' :
                        classes.push('high-confidence-label');
                        break;
                    case 'GOOD' :
                        classes.push('good-confidence-label');
                        break;
                    case 'MEDIUM' :
                        classes.push('medium-confidence-label');
                        break;
                    case 'LOW' :
                        classes.push('low-confidence-label');
                        break;
                    case 'USER' :
                        classes.push('user-confidence-label');
                        break;
                    default :
                        classes.push('default-label');
                        break;
                }

                return classes.join(" ");
            }
        });


        log("Setup tagsinput on " + target.attr("id") + " ok!");

        // check properties
        var source = settings.annotation_source;
        var property;
        var annotations;
        if (isFunction(source)) {
            // source is a function, invoke to get property
            property = source.apply(this, []);
            retrieveZoomaAnnotations(settings, target, property);
        }
        else {
            if (source.is('input')) {
                // if source is an input element, track when user presses enter
                pressEnter($(source), function() {
                    log("Detected enter pressed on " + source.attr("id"));
                    var property = source.val();
                    retrieveZoomaAnnotations(settings, target, property);
                });
            }
            else {
                // otherwise, try to annotate the source directly and put results in the target
                if (isSelector(source)) {
                    retrieveZoomaAnnotations(settings, target, $(source));
                }
                else {
                    retrieveZoomaAnnotations(settings, target, source);
                }
            }
        }

        log("Annotation detection bound ok!");

        // Create and style inner elements
        var container = target.siblings(".bootstrap-tagsinput");
        container.addClass("zooma-annotations-container");
        
        // Spinner   
        var spinner   = createSpinner();
        spinner.prependTo(container);
        spinner.hide();
        
        // Input form
        var customTermInput = target.tagsinput('input');
        customTermInput.attr('data-role','custom-term-input');

        pressEnter(customTermInput, function(e) {
            var element = {};
            element.shortname = customTermInput.val();
            element.confidence = 'USER';
        
            target.tagsinput('add',element);

            customTermInput.val("");
            customTermInput.hide();
        });

        pressEscape(customTermInput, function(e) {
            customTermInput.val("");
            customTermInput.hide();
        });

        customTermInput.val("");
        customTermInput.hide();
        
        
        // Custom term button
        var customTermButton = createCustomTermButton();
        customTermButton.appendTo(container);
        customTermButton.on('click',function(event){
            customTermInput.show();
        });

        container.hide();

        // Setup Item Added Event
        log("Setup annotation added event binding");

        target.on('itemAdded',function(event) {
            var itemName = event.item.shortname;
            var tag = container.find("span.tag:contains('" + itemName + "')");
            
            tag.attr('data-shortcode',itemName);
            setupTooltip(tag,event.item);
        });

        log("Event annotation added bound ok");
        
        return container;
    };

    var setupZoomaWidget = function(target, options) {
        // var settings = extendOptions(options);
        // initLogging(settings);
        // initSpinner();
        // commands['initOptions'].apply();
        var settings =  commands.initOptions(target,options);
        log("Attempting to set up unified zooma widget from " + target.attr('id') + "...");

        // if target is an input box, use as target for autocomplete
        var annotationTarget;
        var _settings;
        if (target.is('input')) {
            annotationTarget = $('<select class="zooma-tags" multiple style="display: none"></select>');
            target.after(annotationTarget);

            // add autocomplete and annotation functions
            var autocompleteContainer = autocomplete(target, settings);
            _settings = $.extend({'annotation_source': target}, settings);
            var annotationContainer = annotate(annotationTarget, _settings);

            // styling
            var container = $('<div class="zooma-container"></div>');
            autocompleteContainer.wrap(container);
            return container;
        }
        else {
            // is a div?
            if (target.is('div')) {
                target.append("Search:");
                // create new input and tags boxes within the target div
                var autocompleteTarget = $('<input type="text" class="zooma-input" />');
                target.append(autocompleteTarget);

                annotationTarget = $('<select class="zooma-tags" multiple style="display: none"></select>');
                target.append(annotationTarget);

                // add autocomplete and annotation functions
                autocomplete(autocompleteTarget, settings);
                _settings = $.extend({'annotation_source': autocompleteTarget}, settings);
                annotate(annotationTarget, _settings);

                // styling
                target.addClass("zooma-container");
                return target;
            }
            else {
                throw "Zooma target must be either <input> or <div> element";
            }
        }
    };



    $.fn.zooma = function(commandOrOptions, options) {
        if (commands[commandOrOptions]) {
            return this.each(function() {
                return commands[commandOrOptions].apply(this, [$(this), options]);
            });
        }
        else {
            try {
                return setupZoomaWidget($(this), commandOrOptions);
            }
            catch (ex) {
                $.error("Invalid usage: jQuery.zooma plugin does not support the command '" + commandOrOptions + "' " +
                        "and this is not an options object either");
            }
        }
    };
})(jQuery,Mustache);
