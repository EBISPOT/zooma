var scrollApis;

$(document).ready(init());

function init() {
    // markup any context help sections
    markupContextHelp();

    // hide stats on init
    $("#zooma-stats").hide();

    // bind change handler to did not map checkbox
    $('#zooma-checkbox').change(function() {
        if ($(this).is(':checked')) {
            $(".unmapped").hide();
        }
        else {
            $(".unmapped").show();
        }
    });

    // bind keydown handler to textarea, so users can type tabs
    $('#zooma-textarea').keydown(function(e) {
        if (e.keyCode == 9) {
            // get caret position/selection
            var start = this.selectionStart;
            var end = this.selectionEnd;

            var $this = $(this);
            var value = $this.val();

            // set textarea value to: text before caret + tab + text after caret
            $this.val(value.substring(0, start)
                              + "\t"
                              + value.substring(end));

            // put caret at right position again (add one for the tab)
            this.selectionStart = this.selectionEnd = start + 1;

            // prevent the focus lose
            e.preventDefault();
        }
    });

    // add progress bar
    var progressbar = $("#progressbar");
    var progressLabel = $(".progress-label");
    progressbar.progressbar({
                                value: 0,
                                change: function() {
                                    if (progressbar.progressbar("value") != false) {
                                        var value = progressbar.progressbar("value");
                                        progressLabel.text(value + "%");
                                    }
                                    else {
                                        progressLabel.text("Uploading terms to ZOOMA...")
                                    }
                                },
                                complete: function() {
                                    progressLabel.text("Complete!");
                                }
                            });

    // retrieve datasources
    populateDatasources();

    // render table contents if there are results in session
    getResults();
}

function initializeScrollpanes() {
    // add customized scroll bars
    scrollApis = [];
    $('.zooma-scrollpane').each(function() {
        scrollApis.push($(this).jScrollPane().data().jsp);
    });
}

function reinitializeScrollpanes() {
    if (scrollApis.length) {
        $.each(scrollApis, function() {
            this.reinitialise();
        });
    }
}

function toggleSearchHelp() {
    // run the effect
    $("#zooma-searches-help").toggle();
    return false;
}

function markupContextHelp() {
    $('.context-help').each(function(index, element) {
        // grab each element with context-help class
        var $element = $(element);

        // get the label and linkify
        var $label = $element.find(".context-help-label").first();
        $label.attr("data-icon", "?");
        $label.addClass("clickable");
        $label.click(function() {
            toggleContextHelp($label);
            return false;
        });

        // get the help content
        var $content = $element.find(".context-help-content").first();
        // style and wrap it
        $content.prepend("<div style='text-align:right;'>" +
                                 "<span onclick='toggleContextHelp(this); return false;' class='icon icon-functional clickable' data-icon='x'>" +
                                 "</span>" +
                                 "</div>");
        $content.wrap("<div class='context-help-wrapper'></div>");
        $content.show();
    });
}

function toggleContextHelp(element) {
    // run the effect
    var $parent = $(element).parents(".context-help").first();
    var $content = $parent.find(".context-help-wrapper").first();
    $content.toggle();
    return false;
}

function populateExamples() {
    $("#zooma-textarea").val("Bright nuclei\nAgammaglobulinemia 2\tphenotype\n" +
        "Reduction in IR-induced 53BP1 foci in HeLa cell\n" +
        "Impaired cell migration with increased protrusive activity\tphenotype\nC57Black/6\tstrain\n" +
        "nuclei stay close together\nRetinal cone dystrophy 3B\tdisease\n" +
        "segregation problems/chromatin bridges/lagging chromosomes/multiple DNA masses\n" +
        "Segawa syndrome autosomal recessive\tphenotype\n" +
        "BRCA1\tgene\nDeafness, autosomal dominant 17\tphenotype\n" +
        "cooked broccoli\tcompound\nAmyloidosis, familial visceral	phenotype\nSpastic paraplegia 10\tphenotype\n" +
        "Epilepsy, progressive myoclonic 1B	phenotype\nBig cells\nCardiomyopathy, dilated, 1S\tphenotype\n" +
        "Long QT syndrome 3/6, digenic	disease\nLung adenocarcinoma\tdisease state\n" +
        "doxycycline 130 nanomolar\tcompound\nleft tibia\torganism part\nCD4-positive\ncerebellum\torganism part\n" +
        "hematology traits\tgwas trait\nnifedipine 0.025 micromolar\tcompound\nMicrotubule clumps\n");
}

function populateDatasources() {
    // clear sorter element if already exists
    var $sorter = $("#zooma-datasource-sorter");
    $sorter.sortable('disable');
    $sorter.html("");

    // retrieve datasources
    $.get('v2/api/sources', function(sources) {
        var datasourceNames = [];
        for (var i = 0; i < sources.length; i++) {
            datasourceNames.push(sources[i].name);
        }

        populateSelector(datasourceNames);
        populateSorter();

        // initialize customized (jscrollpane) scroll bars
        initializeScrollpanes();
    });
}

function populateSelector(datasourceNames) {
    // populate checkboxes
    var selectorContent = "";
    for (var i = 0; i < datasourceNames.length; i++) {
        var datasource = datasourceNames[i];
        selectorContent = selectorContent +
                "<div class=\"grid_8 selectable\">" +
                "<input type=\"checkbox\" name=\"" + datasource + "\" value=\"" + datasource + "\">" +
                datasource +
                "</div>";
    }
    var $selector = $("#datasource-selector");
    $selector.html(selectorContent);
    $selector.find("input").change(function() {
        populateSorter();
        reinitializeScrollpanes();
    });
}

function populateSorter() {
    // retrieve checked selector items
    var datasourceNames = [];
    var $selector = $("#datasource-selector");
    $selector.find("input:checked").each(function() {
        datasourceNames.push($(this).prop("value"));
    });

    // if nothing checked, use all elements
    if (datasourceNames.length == 0) {
        $selector.find("input").each(function() {
            datasourceNames.push($(this).prop("value"));
        });
    }

    // generate unranked and ranked content
    var $unranked = $("#zooma-datasource-unranked");
    var $ranked = $("#zooma-datasource-ranked");

    var unrankedContent = "";
    for (var i = 0; i < datasourceNames.length; i++) {
        var datasource = datasourceNames[i];
        unrankedContent = unrankedContent +
                "<li class=\"ui-state-default sortable unranked\" id=\"" + datasource + "\">" +
                "<span class=\"ui-icon ui-icon-arrowthick-2-n-s\">" +
                "</span>" +
                datasource +
                "</li>";
    }
    $unranked.html(unrankedContent);

    $ranked.html("");

    // make unranked items draggable
    $unranked.find("li").draggable({appendTo: "body", helper: "clone", revert: "invalid"});
//    $unranked.droppable({
//                          activeClass: "ui-state-default",
//                          hoverClass: "ui-state-hover",
//                          accept: ".ranked",
//                          drop: function(event, ui) {
//                              // get content of dropped element
//                              var content = $('<div>').append(ui.draggable.clone()).html();
//                              // change ranked -> unranked and append
//                              $(content).removeClass("ranked")
//                                      .addClass("unranked")
//                                      .appendTo(this);
//                              // remove from original list
//                              $ranked.find("li[id=" + ui.draggable.prop("id") + "]").remove();
//                          }
//                      });

    // make ranked panel droppable
//    $ranked.find("li").draggable({appendTo: "body", helper: "clone", revert: "invalid"});
    $ranked.droppable({
                          activeClass: "ui-state-default",
                          hoverClass: "ui-state-hover",
                          accept: ".unranked",
                          drop: function(event, ui) {
                              // get content of dropped element
                              var content = $('<div>').append(ui.draggable.clone()).html();
                              // change unranked -> ranked and append
                              $(content).removeClass("unranked")
                                      .addClass("ranked")
                                      .appendTo(this);
                              // remove from original list
                              $unranked.find("li[id=" + ui.draggable.prop("id") + "]").remove();
                              reinitializeScrollpanes();
                          }
                      });

    // make ranked panel sortable
    $ranked.sortable({
                         items: "li:not(.placeholder)",
                         sort: function() {
                             // gets added unintentionally by droppable interacting with sortable
                             // using connectWithSortable fixes this,
                             // but doesn't allow you to customize active/hoverClass options
                             $(this).removeClass("ui-state-default");
                             reinitializeScrollpanes();
                         }
                     });
}

function annotate(content) {
    resetSession(function(response) {
        doSearch(jsonifyTextArea(content));
    });
}

function clearAll() {
    resetSession(function(response) {
        $("#zooma-textarea").val("");
        $("#progressbar").hide();
        $("#annotation-results").text("");
        $("#download").hide();
        $("#zooma-stats").hide();
    });
}

function resetSession(callback) {
    $.get('v2/api/services/map/reset', callback);
}

function jsonifyTextArea(content) {
    // reads input from textarea and turns it into a json array
    var json = [];
    var lines = content.split("\n");
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i];
        if (line) {
            var values = line.split("\t");
            var property = {};
            if (values.length > 0) {
                property.propertyValue = values[0];
                if (values.length > 1) {
                    property.propertyType = values[1];
                }
            }
            json.push(property);
        }
    }
    return json;
}

function getRequiredSourcesParam() {
    var selected = [];
    // get child, selected input elements of 'datasource-selector'
    $("#datasource-selector").find("input:checked").each(function() {
        selected.push($(this).prop("value"));
    });

    var required = "";
    if (selected.length > 0) {
        required = "required:[";
        for (var i = 0; i < selected.length - 1; i++) {
            required = required + selected[i] + ",";
        }
        required = required + selected[i] + "]";
    }
    return required;
}

function getPreferredSourcesParam() {
    var sorted = [];
    // get child, selected input elements of 'datasource-selector'
    $("#zooma-datasource-ranked").find("li").each(function() {
        // get the text content of this list item
        sorted.push($(this).text());
    });

    var preferred = "";
    if (sorted.length > 0) {
        preferred = "preferred:[";
        for (var i = 0; i < sorted.length - 1; i++) {
            preferred = preferred + sorted[i] + ",";
        }
        preferred = preferred + sorted[i] + "]";
    }
    return preferred;
}

function doSearch(json) {
    var payload = JSON.stringify(json);
    var requiredSources = getRequiredSourcesParam();
    var preferredSources = getPreferredSourcesParam();
    var url = 'v2/api/services/map';
    if (requiredSources || preferredSources) {
        url = url + '?filter=';
        if (requiredSources) {
            url = url + requiredSources;
        }
        if (preferredSources) {
            url = url + preferredSources;
        }
    }

    $.ajax({
               type: 'POST',
               url: url,
               contentType: 'application/json',
               data: payload,
               beforeSend: function() {
                   $("#progressbar").progressbar({value: false}).show();
               },
               success: function(response) {
                   setTimeout(checkStatus, 100);
               },
               error: function(request, status, error) {
                   alert(error + ": (" + request.responseText + ")");
               }
           });
}

function checkStatus() {
    $.get('v2/api/services/map/status', function(progress) {
        var value = Math.round(progress * 100);
        $("#progressbar").progressbar({value: value});
        if (progress < 1) {
            setTimeout(checkStatus, 500);
        }
        else {
            getResults();
        }
    });
}

function getResults() {
    $.ajax({
               url: 'v2/api/services/map?json',
               dataType: 'json',
               success: function(response) {
                   renderResults(response);
               },
               error: function(request, status, error) {
                   alert(error + ": (" + request.responseText + ")");
               }
           });
}

function renderResults(data) {
    if (data.status != "OK") {
        // if a search failed, alert...
        alert(data.status);
    }
    // ...but still render results

    // clear previous contents
    $ar = $("#annotation-results");
    $ar.html("");

    // result data format:
    /*
     [0] - property type
     [1] - property value
     [2] - matched ontology term label
     [3] - matched ontology term synonyms
     [4] - mapping confidence
     [5] - matched ontology term "ID" (i.e. fragment)
     [6] - matched ontology URI
     [7] - datasource
     */

    // render new payload
    var payload = data.data;
    if ($.isEmptyObject(payload) == false) {
        var prop_automatic = 0;
        var prop_curation = 0;
        var prop_unmapped = 0;

        var aux_type = "";
        var aux_value = "";

        var tableContent = "";
        var spanTo = -1;
        for (var i = 0; i < payload.length; i++) {
            var result = payload[i];
            var row;
            var rowspan = 1;
            if (result[4] == "High") {
                row = "<tr class='automatic'>";

                prop_automatic++;
                aux_type = result[0];
                aux_value = result[1];
            }
            else if (result[4] == "Good" || result[4] == "Medium" || result[4] == "Low") {
                row = "<tr class='curation'>";

                if (result[0] != aux_type || result[1] != aux_value) {
                    prop_curation++;
                    aux_type = result[0];
                    aux_value = result[1];
                }


                // has a previous row spanned multiple rows, or are we ok to check?
                if (spanTo <= i) {
                    // might have multiple results, work out how many rows to span
                    for (var j = (i + 1); j < payload.length; j++) {
                        var nextResult = payload[j];
                        if (nextResult[0] == result[0] && nextResult[1] == result[1]) {
                            rowspan++;
                            spanTo = j;
                        }
                        else {
                            break;
                        }
                    }
                }
            }
            else {
                row = "<tr class='unmapped'>";

                prop_unmapped++;
                aux_type = result[0];
                aux_value = result[1];
            }

            // does this result span multiple rows?
            if (rowspan > 1) {
                // if so, render with rowspan
                row = row + "<td rowspan='" + rowspan + "' style='vertical-align: middle'>" + result[0] + "</td>";
                row = row + "<td rowspan='" + rowspan + "' style='vertical-align: middle'>" + result[1] + "</td>";
            }
            else {
                // if not, do we need to skip this column (due to previous row spanning)?
                if (spanTo < i) {
                    // render normally
                    row = row + "<td>" + result[0] + "</td>";
                    row = row + "<td>" + result[1] + "</td>";
                }
            }
            row = row + "<td>" + result[2] + "</td>";
            row = row + "<td>" + result[4] + "</td>";
            if (result[5] != "N/A") {
                // multiple mappings will be comma separated
                if (result[5].indexOf(", ") == -1) {
                    // no comma separation, linkify entire field
                    row = row + "<td>" + linkify(result[6] + result[5], result[5]) + "</td>";
                }
                else {
                    // comma separation, linkify each token
                    var termIDs = result[5].split(", ");
                    var ontologyURIs = result[6].split(", ");

                    // should be same number of IDs and URIs
                    if (termIDs.length != ontologyURIs.length) {
                        alert("Failed to read mapping result row " + i + ": there is a different number " +
                                      "of mapping results and ontologies.  Data was:\n" + result + ".");
                    }
                    else {
                        var links = "";
                        var l = termIDs.length - 1;
                        for (var k = 0; k < l; k++) {
                            var termID = termIDs[k].trim();
                            var ontologyURI = ontologyURIs[k];
                            links += linkify(ontologyURI + termID, termID) + ",<br />";
                        }
                        links += linkify(ontologyURIs[l] + termIDs[l], termIDs[l]);
                        row = row + "<td>" + links + "</td>";
                    }
                }
            }
            else {
                row = row + "<td>" + result[5] + "</td>";
            }
            if (result[7] != "N/A") {
                var href;
                if (result[7] == "http://www.ebi.ac.uk/gxa") {
                    href =
                            "http://www.ebi.ac.uk/gxa/query?condition=" +
                                    encodeURIComponent(result[1]);
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='//www.ebi.ac.uk/gxa/resources/images/ExpressionAtlas_logo_web.png' " +
                            "alt='Expression Atlas' style='height: 22px;'/> Expression Atlas</a></td>";
                }
                else if (result[7] == "http://www-test.ebi.ac.uk/gxa") {
                    href =
                            "http://www-test.ebi.ac.uk/gxa/qrs?gprop_0=&gnot_0=&gval_0=%28all+genes%29&fact_1=&fexp_1=UP_DOWN&fmex_1=&fval_1=" +
                                    encodeURIComponent(result[1]) +
                                    "&view=hm&searchMode=simple";
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='//www.ebi.ac.uk/gxa/resources/images/ExpressionAtlas_logo_web.png' " +
                            "alt='Expression Atlas' style='height: 22px;'/> GXA</a></td>";
                }
                else if (result[7] == "http://www.ebi.ac.uk/arrayexpress") {
                    href = "http://www.ebi.ac.uk/arrayexpress/experiments/search.html?query=" +
                            encodeURIComponent(result[1]);
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='//www.ebi.ac.uk/sites/ebi.ac.uk/files/styles/icon/public/resource/logo/aelogo.jpg' " +
                            "alt='ArrayExpress' style='height: 22px;'/> ArrayExpress</a></td>";
                }
                else if (result[7] == "http://www.ebi.ac.uk/efo/efo.owl") {
                    href = "http://www.ebi.ac.uk/efo/search?query=" +
                            encodeURIComponent(result[2]) +
                            "&submitSearch=Search";
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='//www.ebi.ac.uk/sites/ebi.ac.uk/files/styles/thumbnail/public/resource/logo/EFO_logo_0.png' " +
                            "alt='EFO' style='height: 22px;'/> EFO</a></td>";
                }
                else if (result[7] == "http://www.genome.gov/gwastudies") {
                    href = "http://www.genome.gov/gwastudies/#searchForm";
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='images/nhgri.png' " +
                            "alt='GWAS' style='height: 22px;'/> GWAS</a></td>";
                }
                else if (result[7] == "http://omia.angis.org.au") {
                    href = result[5] + result[4];
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='images/omia.png' " +
                            "alt='OMIA' style='height: 22px;'/> OMIA</a></td>";
                }
                else if (result[7] == "http://omim.org") {
                    href = result[5] + result[4];
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='images/omim.gif' " +
                            "alt='OMIM' style='height: 22px;'/> OMIM</a></td>";
                }
                else if (result[7] == "http://www.ebi.ac.uk/fg/sym") {
                    href = result[7];
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='images/CelPh_logo.gif' " +
                            "alt='SysMicro' style='height: 22px;'/> SysMicro</a></td>";
                }
                else if (result[7] == "http://www.ebi.ac.uk/cmpo/cmpo.owl") {
                    row = row + "<td><a href='http://www.ebi.ac.uk/cmpo' target='_blank'>" +
                            "<img src='images/cmpo.png' " +
                            "alt='CMPO' style='height: 22px;'/> CMPO</a></td>";
                }
                else if (result[7] == "http://www.orpha.net/ontology/orphanet.owl") {
                    row = row + "<td><a href='http://www.orphadata.org/cgi-bin/index.php' target='_blank'>" +
                            "<img src='images/orphanet.png' " +
                            "alt='ORDO' style='height: 20px;'/> ORDO</a></td>";
                }
                else if (result[7] == "http://www.biomedbridges.eu/workpackages/wp7-0") {
                    row = row + "<td><a href='http://www.biomedbridges.eu/workpackages/wp7-0' target='_blank'>" +
                            "<img src='images/bmb.png' " +
                            "alt='BMB-WP7' style='height: 20px;'/> BMB-WP7</a></td>";
                }
                else if (result[7] == "http://purl.obolibrary.org/obo/clo.owl") {
                    row = row + "<td><a href='http://www.clo-ontology.org/' target='_blank'>" +
                            "<img src='images/clo.jpg' " +
                            "alt='CLO' style='height: 20px;'/> CLO</a></td>";
                }
                else if (result[7] == "http://purl.obolibrary.org/obo/eo.owl") {
                    row = row + "<td><a href='http://archive.gramene.org/plant_ontology/index.html#eo' target='_blank'>" +
                            "<img src='images/gramene_logo.png' " +
                            "alt='PECO' style='height: 20px;'/> PECO</a></td>";
                }
                else if (result[7] == "http://www.ebi.ac.uk/chembl") {
                    row = row + "<td><a href='http://www.ebi.ac.uk/chembl' target='_blank'>" +
                            "<img src='images/new_chembl_logo_v2.png' " +
                            "alt='ChEMBL' style='background-color: #70BDBD; height: 20px;'/> ChEMBL</a></td>";
                }
                else if (result[7] == "http://www.uniprot.org") {
                    row = row + "<td><a href='http://www.uniprot.org' target='_blank'>" +
                            "<img src='images/uniprot_logo.gif' " +
                            "alt='UniProt' style='height: 20px;'/> UniProt</a></td>";
                }
                else if (result[7] == "http://purl.obolibrary.org/obo/mp.owl") {
                    row = row + "<td><a href='http://www.informatics.jax.org/searches/MP_form.shtml' target='_blank'>" +
                            "<img src='images/mpi_logo.gif' " +
                            "alt='MP' style='height: 20px;'/> MP</a></td>";
                }
                else {
                    row = row + "<td>" + result[7] + "</td>";
                }
            }
            else {
                row = row + "<td>" + result[7] + "</td>";
            }
            row = row + "</tr>";
            tableContent = tableContent + row;
        }
        $ar.append(tableContent);
        $("#download").show();

        prop_total = prop_automatic + prop_curation + prop_unmapped;
        if (prop_total > 0) {
            var $zoomaStats = $("#zooma-stats");
            var text_curation = "";
            if (prop_curation == 1) {
                text_curation = "requires curation";
            }
            else {
                text_curation = "require curation";
            }

            var statsContent = "Stats: &nbsp;&nbsp;&nbsp;&nbsp; " + prop_total + " properties " +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    prop_automatic + " automatic" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; " +
                    prop_curation + " " + text_curation + " " +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; " +
                    prop_unmapped + " unmapped";
            $zoomaStats.find("#stats").html(statsContent);
            $zoomaStats.show();
        }
        else {
            $("#zooma-stats").hide();
        }
    }
    else {
        $("#download").hide();
    }

    // work out whether to show or hide
    if ($("#zooma-checkbox").is(':checked')) {
        $(".unmapped").hide();
    }
    else {
        $(".unmapped").show();
    }
}

function linkify(url, name) {
    return "<a href=\"" + url + "\" target=\"_blank\"><span title=\"" + url + "\">" + name + "</span></a>";
}