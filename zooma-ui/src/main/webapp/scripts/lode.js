

var appname = "ZOOMA SPARQL endpoint";
var sparqlEndpoint = "v2/api/query";
var resultsPerPage = 25;

var logging = false;

var defaultQuery = "SELECT DISTINCT ?zoomaAnnotation WHERE {\n  ?zoomaAnnotation a oac:DataAnnotation\n}";

var _namespaces = {};
var tableid = "results-table";

var nextUrl;
var prevUrl;

function init() {

    $(document).ready(function() {
        resetPage();
        setNamespaces(lodeNamespacePrefixes);
        setExampleQueries ();
        querySparql();
        if (!logging) {
            $('#lode-log').hide();
        }
    });
}

function submitQuery() {
    $('#queryform').submit();
}

function setDefaultQuery() {
    $('#textarea').val (_getPrefixes() + "\n" + defaultQuery);
}
function querySparql () {

    var match = document.location.href.match(/\?(.*)/);
    var queryString = match ? match[1] : '';

    var querytext = null;
    var limit = resultsPerPage;
    var offset = 0;

    // if no query just return and wait for one
    if (!queryString) {
        setDefaultQuery();
        return;
    }

    // get the query string and execute
    if (queryString.match(/query=/)) {
        querytext = this._betterUnescape(queryString.match(/query=([^&]*)/)[1]);
        var query = _getPrefixes() + querytext;
    }

    if (queryString.match(/limit=/)) {
        limit = this._betterUnescape(queryString.match(/limit=([0-9]*)/)[1]);
        resultsPerPage = limit;
        $('#limit').val(limit);
    }

    if (queryString.match(/offset=/)) {
        offset = this._betterUnescape(queryString.match(/offset=([0-9]*)/)[1]);
        $('#offset').val(offset);
    }

    clearErrors();
    if (!querytext) {
        querytext = query;
    }

    // GET THE RENDERING
    var rendering = "HTML";
    if (queryString.match(/render=/)) {
        rendering = this._betterUnescape(queryString.match(/render=([^&]*)/)[1]);
    }

    $('#textarea').val(querytext);

    var exp = /^\s*(?:PREFIX\s+\w*:\s+<[^>]*>\s*)*(\w+)\s*.*/i;
    var match = exp.exec(querytext);
    var successFunc;
    var requestHeader;


    if (match) {
        if (match[1].toUpperCase() == 'CONSTRUCT' || match[1].toUpperCase() == 'DESCRIBE') {

            if (rendering.match(/HTML/)) {
                requestHeader = "text/plain";
                successFunc = function(model) {
                    hideBusyMessage();
                    renderGraphQuery(model, tableid);
                };
            }
            else if (rendering.match(/RDF/)) {
                location.href = sparqlEndpoint + "?query=" + encodeURIComponent(querytext) + "&format=RDF/XML";
            }
            else if (rendering.match(/N3/)) {
                location.href = sparqlEndpoint + "?query=" + encodeURIComponent(querytext) + "&format=N3";
            }
            else  {
                displayError("You can only render graph queries in either HTML, RDF/XML, or RDF/N3 format")
                return;
            }
        }
        else if (match[1].toUpperCase() == 'DELETE' || match[1].toUpperCase() == 'UPDATE') {
            displayError("UPDATE or DELETE queries not allowed")
            return;
        }
        else {
            if (rendering.match(/HTML/)) {
                requestHeader = "application/sparql-results+json";
                successFunc = function(json) {
                    hideBusyMessage();
                    renderSparqlResultJsonAsTable(json, tableid);
                };
            }
            else if (rendering.match(/^XML/)) {
                location.href = sparqlEndpoint + "?query=" + encodeURIComponent(querytext) + "&format=XML&limit=" + limit + "&offset=" + offset;
            }
            else if (rendering.match(/JSON/)) {
                location.href = sparqlEndpoint + "?query=" + encodeURIComponent(querytext) + "&format=JSON&limit=" + limit + "&offset=" + offset;
            }
            else  {
                displayError("You can only render SELECT queries in either HTML, XML or JSON format")
                return;
            }
        }
    }

    // about to execute query
    displayBusyMessage();
    setNextPrevUrl(querytext, limit, offset);
    $.ajax ( {
        type: 'GET',
        url: sparqlEndpoint + "?" + queryString,
        headers: {
            Accept: requestHeader
        },
        success: successFunc,
        error: function (request, status, error) {
            hideBusyMessage();
            displayError(request.responseText);
        }
    });
}

function setNextPrevUrl (queryString, limit, offset) {

    nextUrl = "query=" + encodeURIComponent(queryString) + "&limit=" + limit + "&offset=" + (parseInt(offset) + parseInt(resultsPerPage));
    if (offset >= resultsPerPage) {
        prevUrl = "query=" + encodeURIComponent(queryString) + "&limit=" + limit + "&offset=" + (parseInt(offset) - parseInt(resultsPerPage));
    }
    else {
        prevUrl = "query=" + encodeURIComponent(queryString) + "&limit=" + limit + "&offset=0";
    }
}


function renderGraphQuery (graph, tableid) {

    var rows = graph.split (/\n/);
    $("#" + tableid).html("");

    var header = createTableHeader(['Subject', 'Predicate', 'Object']);
    $("#" + tableid).append(header);

    // parse an n-triples file, markup links
    for (var x = 0 ; x < rows.length; x++ ) {
        // check for commented lines #

        // remove line endings
        var row = rows[x].replace (/\s+.$/, '');

        row = row.replace (/\^\^\<.*\>$/, '');
        var cells = row.split(/["<>]+\s+[<>"]+/);

        // remove data types

        // ignore blank nodes
        if (cells.length == 3) {
            var tr =$('<tr />');
            for (var j = 0; j < cells.length; j ++) {
                var cell = cells[j].replace (/^["<]/, '');
                cell = cell.replace (/[">]$/, '');
                var td = $('<td />');
                var resource = cell;
                resource = resource.replace (/^["<]/, '');
                resource = resource.replace (/^[">]\s+\.$/, '');
                if (resource.match(/^(https?|ftp|mailto|irc|gopher|news):/)) {
                    var shortForm =  _toQName(resource);
                    if (shortForm == null) {
                        shortForm = resource;
                    }

                    var internalHref = "?query=describe <" +encodeURIComponent(resource) + ">";

                    var linkSpan  = $('<span/>');
                    var img = $('<img />');
                    img.attr('src', 'images/external_link.png');
                    img.attr('alt', '^');
                    img.attr('title', 'Resolve URI on the web');

                    var ea = $('<a />');
                    ea.attr('href', resource);
                    ea.attr('class', 'externallink');
                    ea.attr('target', 'blank');
                    ea.append(img);


                    var a = $('<a />');
                    a.attr('href',internalHref);
                    a.text(shortForm);
                    linkSpan.append(a);
                    linkSpan.append("&nbsp;");
                    linkSpan.append(ea);
                    td.append(linkSpan);
                }
                else {
                    td.append (resource)
                }
                tr.append(td);
            }
        }
        $("#" + tableid).append(tr);
    }
}

function displayPagination()  {


    var prevA = $('<a></a>');
    prevA.attr('href',"?" + prevUrl);
    prevA.attr('class',"pag prev");
    prevA.text("Previous")

    var nextA = $('<a></a>');
    nextA.attr('href',"?" + nextUrl);
    nextA.attr('class',"pag next");
    nextA.text("Next")

    var pagtext = $('<span></span>');
    pagtext.attr('class', "pag pagmes");
    pagtext.text('');
    pagtext.text($('#limit').val() + ' results per page (offset ' + $('#offset').val() + ")")
    $('#pagination').append(prevA);
    $('#pagination').append(pagtext);
    $('#pagination').append(nextA);
    $('#pagination').show();

}

function renderSparqlResultJsonAsTable (json, tableid) {
    log("sparql query rendering json")

    // clear existing content
    $("#" + tableid).html("");

    var _json = json;
    var _variables = _json.head.vars;
    var _results = _json.results.bindings;

    var header = createTableHeader(_variables);

    $("#" + tableid).append(header);

    displayPagination();

    for (var i = 0; i < _results.length; i++) {
        var row =$('<tr />');
        var binding = _results[i];
        for (var j = 0 ; j < _variables.length; j++) {
            var varName = _variables[j];
            var formattedNode = _formatNode(binding[varName], varName);
            var cell = $('<td />');
            cell.append (formattedNode);
            row.append(cell);
        }
        $("#" + tableid).append(row);
    }

}

function _formatNode (node, varName) {
    if (!node) {
        return _formatUnbound(node, varName);
    }
    if (node.type == 'uri') {
        return _formatURI(node, varName);
    }
    if (node.type == 'bnode') {
        return _formatBlankNode(node, varName);
    }
    if (node.type == 'literal') {
        return _formatPlainLiteral(node, varName);
    }
    if (node.type == 'typed-literal') {
        return _formatTypedLiteral(node, varName);
    }
    return '???';
}

function _formatURI (node, varName) {

    var internalHref = "?query=describe <" +encodeURIComponent(node.value) + ">";
    var title = node.value;
    var className = 'graph-link';
    var shortForm =  _toQName(node.value);
    if (!shortForm) {
        shortForm = "<" + node.value + ">";
    }

    // handle external link
    var xref = node.value;
//
    match = node.value.match(/^(https?|ftp|mailto|irc|gopher|news):/);
    if (match) {
        var linkSpan  = $('<span/>');
        var img = $('<img />');
        img.attr('src', 'images/external_link.png');
        img.attr('alt', '^');
        img.attr('title', 'Resolve URI on the web');

        var ea = $('<a />');
        ea.attr('href', node.value);
        ea.attr('class', 'externallink');
        ea.attr('target', 'blank');
        ea.append(img);

        var a = $('<a />');
        a.attr('href',internalHref);
        a.attr('class',className);
        a.text(shortForm);

        linkSpan.append(a);
        linkSpan.append("&nbsp;");
        linkSpan.append(ea);

        return linkSpan;

    }

    return xref;

}

function setExampleQueries() {

    $('#queries_list').empty();
    if (exampleQueries != null) {

        if (exampleQueries.length > 0) {

            for (var x = 0;x <exampleQueries.length; x ++) {

                // queries_list
                var shortname = exampleQueries[x].shortname;
                var desc = exampleQueries[x].description;
                var query= exampleQueries[x].query;

                log ("query:" + shortname + " " + desc + " " + query) ;
                var a = $('<a></a>');
                a.attr ('id', x);
                a.attr ('class', 'query-short');
                a.text (shortname);

                $(a).click(function () {
                    _setTextAreQuery(this)
                });

                var li = $('<li></li>');
                li.append(a);
                li.append($('<span></span>').append( '&nbsp;-&nbsp;' + desc));
                $('#queries_list').append(li);

            }
        }

    }

}

function _setTextAreQuery(anchor) {
    $('#textarea').val (_getPrefixes() + "\n\n" + exampleQueries[anchor.id].query);
}

function _formatPlainLiteral (node, varName) {
    var text = '"' + node.value + '"';
//    if (node['xml:lang']) {
//        text += '@' + node['xml:lang'];
//    }
    return document.createTextNode(text);
}

function _formatTypedLiteral (node, varName) {
    var text = '"' + node.value + '"';
//    if (node.datatype) {
//        text += '^^' + this._toQNameOrURI(node.datatype);
//    }
    if (this._isNumericXSDType(node.datatype)) {
        var span = document.createElement('span');
        span.title = text;
        span.appendChild(document.createTextNode(node.value));
        return span;
    }
    return document.createTextNode(text);
}

function _formatBlankNode (node, varName) {
    return document.createTextNode('_:' + node.value);
}

function _formatUnbound (node, varName) {
    var span = document.createElement('span');
    span.className = 'unbound';
    span.title = 'Unbound'
    span.appendChild(document.createTextNode('-'));
    return span;
}

function _toQName (uri) {
    for (prefix in _namespaces) {
        var nsURI = _namespaces[prefix];
        if (uri.indexOf(nsURI) == 0) {
            return prefix + ':' + uri.substring(nsURI.length);
        }
    }
    return null;
}

function _toQNameOrURI (uri) {
    var qName = this._toQName(uri);
    return (qName == null) ? '<' + uri + '>' : qName;
}

function _isNumericXSDType (datatypeURI) {
    for (i = 0; i < this._numericXSDTypes.length; i++) {
        if (datatypeURI == this._xsdNamespace + this._numericXSDTypes[i]) {
            return true;
        }
    }
    return false;
}

var _xsdNamespace = 'http://www.w3.org/2001/XMLSchema#';
var _numericXSDTypes = ['long', 'decimal', 'float', 'double', 'int',
    'short', 'byte', 'integer', 'nonPositiveInteger', 'negativeInteger',
    'nonNegativeInteger', 'positiveInteger', 'unsignedLong',
    'unsignedInt', 'unsignedShort', 'unsignedByte'];


function createTableHeader (names) {
    var htmlString = "";
    for (var i = 0 ; i < names.length; i++) {
        log(names[i]);
        htmlString +="<th>" + names[i] + "</th>";
    }
    return htmlString;
}

function reloadPage() {
    var match = document.location.href.match(/(.*)\?.*/);
    document.location = match[1];
}



function _getPrefixes () {
    var prefixes = '';
    for (prefix in this._namespaces) {
        var uri = this._namespaces[prefix];
        prefixes = prefixes + 'PREFIX ' + prefix + ': <' + uri + '>\n';
    }
    return prefixes;
}

function setNamespaces (namespaces) {
    this._namespaces = namespaces;
}

function _betterUnescape (s) {
    return unescape(s.replace(/\+/g, ' '));
}


function resetPagination() {
    $('#pagination').empty();
    $('#pagination').hide();
    $('#limit').val(resultsPerPage);
    $('#offset').val(0);
    $('#' +tableid).empty();
}

function resetPage() {
    displayAppName();
    setDefaultQuery();
    displaySparqlEndpoint();
    resetPagination();
    hideBusyMessage();
    clearErrors();
}

function clearErrors() {
    $("#error-text").text("");
    $("#error-div").hide();
}

function displayAppName() {
    if (appname == null) {
        appname = "Cool SPARQL App";
    }
    $('#appname').text (appname);
}
function displaySparqlEndpoint() {
    $("#sparql-endpoint-url").text(sparqlEndpoint);
}

function displayError(message) {
    log(message);
    $("#error-text").text(message);
    $("#error-div").show();
}


function displayBusyMessage() {
    $("#query-executing-spinner").show();
}
function hideBusyMessage() {
    $("#query-executing-spinner").hide();
}


function log(msg) {
    $('#logmessage').append(msg);
}
