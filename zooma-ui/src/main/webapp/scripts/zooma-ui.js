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
                                    document.getElementById("zooma-results").scrollIntoView();
                                }
                            });

    // retrieve datasources
    populateDatasources();

    // render table contents if there are results in session
    getResults();
}

$(document).ready(function() {
    $('.nav-toggle').click(function(){
        //get collapse content selector
        var collapse_content_selector = $("#collapse1");

        //make the collapse content to be shown or hide
        $(collapse_content_selector).toggle(function(){
        });
    });

});


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

$(function() {
    $( "#accordion" ).accordion({
        collapsible: true,
        heightStyle: "content",
        active: false,
        activate:function(event, ui ){
            // Grab current anchor value
            var currentAttrValue = $(this).attr('name');
            if (currentAttrValue == "accordion-1"){
                populateOntologies();
            }
            reinitializeScrollpanes();
        }
    });
});

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
var datasourceNames = [];
var searchableOntoNames = [];
var ontologyPrefixes = [];
var loadedOntologyURIs = [];
var nameDescriptionMap = {}; //Map
var nameTitleMap = {};
var uriNameMap = {};

function populateDatasources() {
    // clear sorter element if already exists
    var $sorter = $("#zooma-datasource-sorter");
    $sorter.sortable('disable');
    $sorter.html("");

    // retrieve datasources
    $.get('v2/api/sources', function(sources) {
        datasourceNames = [];
        searchableOntoNames = [];
        for (var i = 0; i < sources.length; i++) {
            if (sources[i].type == "DATABASE") {
                var name = sources[i].name;

                if(name == "sysmicro"){
                    datasourceNames.push("CellularPhenoTypes");
                    var desc = "<p><b>Cellular Phenotype Database</b><br>The Cellular Phenotype database provides easy access " +
                        "to phenotypic data derived from high-throughput screening, facilitating data sharing and integration.</p>" +
                        "<p><b>database name: 'sysmicro'</b><br><a href='" + sources[i].uri + "' target='_blank'>" + sources[i].uri + "</a></p>";
                    nameDescriptionMap["CellularPhenoTypes"] = desc;
                }
                else if (name == "ebisc"){
                    datasourceNames.push("EBiSC");
                    var desc = "<p><b>Cell Line Catalogue</b><br>iPSC line catalogue</p>" +
                        "<p><b>database name: 'ebisc'</b><br><a href='" + sources[i].uri + "' target='_blank'>" + sources[i].uri + "</a></p>";
                    nameDescriptionMap["EBiSC"] = desc;
                } else if (name == "cttv"){
                    datasourceNames.push("OpenTargets");
                    var desc = "<p><b>Open Targets</b><br>Open Targets is a public-private initiative to " +
                        "generate evidence on the validity of therapeutic targets based on genome-scale experiments and analysis.</p>" +
                        "<p><b>database name: 'cttv'</b><br><a href='" + sources[i].uri + "' target='_blank'>" + sources[i].uri + "</a></p>";
                    nameDescriptionMap["OpenTargets"] = desc;
                } else if (name == "uniprot"){
                    datasourceNames.push("UniProt");
                    var desc = "<p><b>UniProt</b><br>A comprehensive, high quality and freely accessible resource of protein sequence and functional information.</p>" +
                        "<p><b>database name: 'uniprot'</b><br><a href='" + sources[i].uri + "' target='_blank'>" + sources[i].uri + "</a></p>";
                    nameDescriptionMap["UniProt"] = desc;
                } else if (name == "eva-clinvar"){
                    datasourceNames.push("ClinVar");
                    var desc = "<p><b>European Variation Archive</b><br>The European Variation Archive is an open-access database " +
                        "of all types of genetic variation data from all species.</p>" +
                        "<p><b>database name: 'eva-clinvar'</b><br><a href='" + sources[i].uri + "' target='_blank'>" + sources[i].uri + "</a></p>";
                    nameDescriptionMap["ClinVar"] = desc;
                } else if (name == "gwas"){
                    datasourceNames.push("GWAS");
                    var desc = "<p><b>GWAS</b><br> A Catalog of Published Genome-Wide Association Studies.</p>" +
                        "<p><b>database name: 'gwas'</b><br><a href='//www.ebi.ac.uk/gwas' target='_blank'>www.ebi.ac.uk/gwas</a></p>";
                    nameDescriptionMap["GWAS"] = desc;
                } else if (name == "atlas") {
                    datasourceNames.push("ExpressionAtlas");
                    var desc = "<p><b>Expression Atlas</b><br>The Expression Atlas provides information on gene expression patterns under different biological conditions.</p>" +
                        "<p><b>DB name: 'atlas'</b><br><a href='" + sources[i].uri + "' target='_blank'>" + sources[i].uri + "</a></p>";
                    nameDescriptionMap["ExpressionAtlas"] = desc;
                } else if (name == "metabolights"){
                datasourceNames.push("Metabolights");
                var desc = "<p><b>MetaboLights</b><br>is a database for Metabolomics experiments and derived information. The database is cross-species, cross-technique and covers metabolite structures and their reference spectra as well as their biological roles, locations and concentrations, and experimental data from metabolic experiments.</p>" +
                    "<p><b>DB name: 'metabolights'</b><br><a href='" + sources[i].uri + "' target='_blank'>" + sources[i].uri + "</a></p>";
                nameDescriptionMap["Metabolights"] = desc;
                } else if (name == "ukbiobank"){
                datasourceNames.push("UKBiobank");
                var desc = "<p><b>UK Biobank</b><br>is a large long-term biobank study in the United Kingdom which is investigating the respective contributions of genetic predisposition and environmental exposure to the development of disease. This Zooma datasets contains mappings for UK BioBank traits to the Experimental Factor Ontology</p>" +
                    "<p><b>DB name: 'ukbiobank'</b><br><a href='" + sources[i].uri + "' target='_blank'>" + sources[i].uri + "</a></p>";
                nameDescriptionMap["Metabolights"] = desc;
                } else if (name == "cbi"){
                    datasourceNames.push("CBI");
                    var desc = "<p><b>Crop Bioinformatics Initiative</b><br>The CBI datasource contains a series of mappings designed to enable high-throughput " +
                        "ontology annotation of plant-specific sample data. " +
                        "These mappings are derived from the most commonly observed attributes used to describe plant samples in the " +
                        "BioSamples database that could not previously be mapped to ontology terms. " +
                        "This work was part of the <a href='//gtr.rcuk.ac.uk/projects?ref=BB%2FM018458%2F1' target='_blank'>\"Big Data Infrastructure for Crop Genomics\"</a> project</p>" +
                        "<p><b>DB name: 'cbi'</b><br><a href='" + sources[i].uri + "' target='_blank'>" + sources[i].uri + "</a></p>";
                    nameDescriptionMap["CBI"] = desc;
                } else if (name == "clinvar-xrefs"){
                    datasourceNames.push("ClinVarXRefs");
                    var desc = "<p><b>ClinVar</b><br>ClinVar aggregates information about genomic variation and its relationship to human health.</p>" +
                        "<p><b>DB name: 'clinvar-xrefs'</b><br><a href='" + sources[i].uri + "' target='_blank'>" + sources[i].uri + "</a></p>";
                    nameDescriptionMap["ClinVarXRefs"] = desc;
                } else {
                    datasourceNames.push(name);
                    nameDescriptionMap[name] =  "No description.";
                }
                uriNameMap[sources[i].uri] =  sources[i].name;
            } else if (sources[i].type == "ONTOLOGY"){
                searchableOntoNames.push(sources[i].title + " (" + sources[i].name + ")");
                ontologyPrefixes.push(sources[i].name);
                loadedOntologyURIs.push(sources[i].uri);
                var desc = "<p><b>" + sources[i].title + "</b><br>" + sources[i].description + "</p>" +
                    "<p><a href='//www.ebi.ac.uk/ols/ontologies/" + sources[i].name + "' target='_blank'>view ontology in OLS</a></p>";
                nameDescriptionMap[sources[i].name] = desc;
                uriNameMap[sources[i].uri] =  sources[i].name;
            }

        }

        populateDatasourceSelector();
        populateOntologySelector();
        populateSorter();

        // initialize customized (jscrollpane) scroll bars
        initializeScrollpanes();
    });
}

function getSourceDescription(name){
    return nameDescriptionMap[name.toString()];
}

function getSourceTitle(name){
    return nameTitleMap[name.toString()];
}

function populateOntologySelector() {
// setup autocomplete function pulling from searchableOntoNames[] array
    if (searchableOntoNames.length == 0){
        $('input.input').val("No ontologies available at the moment.");
        $('#autocomplete').prop( "disabled", true );
    } else {
        $('#autocomplete').autocomplete({
            lookup: searchableOntoNames,
            onSelect: function (suggestion) {
                //clean ontology prefix from the title and the parenthesis
                var firstSplitArray = suggestion.value.split("(");
                var firstSplit = firstSplitArray[firstSplitArray.length - 1];
                var ontology = firstSplit.split(")")[0];
                populateOntologies(ontology);
            }
        });
    }
}

function populateOntologies(instertNew){
    //add the selected suggestion to the output value
    var datasourceNames = [];
    var datasourceNamesChecked = [];

    var $selector = $("#selected-ontologies");
    $selector.find("input").each(function() {
        datasourceNames.push($(this).prop("value"));
    });

    $selector.find("input:checked").each(function() {
        datasourceNamesChecked.push($(this).prop("value"));
    });

    //push the new ontology selected, if not in list
    if (undefined != instertNew) {
        if (datasourceNames.length == 0) {
            datasourceNamesChecked.push(instertNew);
            datasourceNames.push(instertNew);
        } else if (datasourceNames.indexOf(instertNew) == -1) { //not found
            datasourceNamesChecked.push(instertNew);
            datasourceNames.push(instertNew);
        }
    }

    var selectorContent = "";
    for (var i = 0; i < datasourceNames.length; i++) {
        var datasource = datasourceNames[i];
        if (datasourceNamesChecked.indexOf(datasource) > -1){ //if found in checked then put it in as already checked
            selectorContent = selectorContent +
                "<li style=\"margin-bottom: 1px;\">" +
                "<input type=\"checkbox\" name=\"" + datasource + "\" value=\"" + datasource + "\" checked>" +
                "<a id=\"description-" + datasource + "\" onclick=\"showDesc(this)\" onmouseout=\"hideDesc(this)\" style=\"cursor:help;\">" +
                datasource +
                "</a></li>";
        } else {
            selectorContent = selectorContent +
                "<li style=\"margin-bottom: 1px;\">" +
                "<input type=\"checkbox\" name=\"" + datasource + "\" value=\"" + datasource + "\" >" +
                "<a id=\"description-" + datasource + "\" onclick=\"showDesc(this)\" onmouseout=\"hideDesc(this)\" style=\"cursor:help;\">" +
                datasource +
                "</a></li>";
        }
    }

    var $selector = $("#selected-ontologies");
    $selector.html(selectorContent);

    reinitializeScrollpanes();

}

function deselectAllOntologies(){

    var checked = false;
    var $selector = $("#select-none-ontologies");

    $selector.find("input:checked").each(function() {
        if($(this).prop("value") == "Select None"){
            checked = true;
        }
    });

    if (checked){
        //disable the autocomplete widget if selected and remove all ontologies from checklist
        $("#selected-ontologies").html("");
        $('#autocomplete').prop( "disabled", true );
        reinitializeScrollpanes();
    } else {
        $('#autocomplete').prop( "disabled", false );
        populateOntologySelector();
    }
}


function deselectAllDatasources(){
    var checked = false;
    var $selector = $("#select-none-datasources");

    $selector.find("input:checked").each(function() {
        if($(this).prop("value") == "Select None"){
            checked = true;
        }
    });

    if (checked){
        // re-populate uncheck all datasources and disable them
        var datasourceNames = [];
        var $selector = $("#datasource-selector");
        $selector.find("input").each(function() {
            datasourceNames.push($(this).prop("value"));
        });

        var selectorContent = "";
        for (var i = 0; i < datasourceNames.length; i++) {
            var datasource = datasourceNames[i];

            selectorContent = selectorContent +
                "<li style=\"margin-bottom: 1px;\">" +
                "<input type=\"checkbox\" name=\"" + datasource + "\" value=\"" + datasource + "\" onclick=\"return false;\" >" +
                "<a id=\"description-" + datasource + "\" onclick=\"showDesc(this)\"  onmouseout=\"hideDesc(this)\" style=\"cursor:help;\">" +
                datasource +
                "</a></li>";

        }
        var $selector = $("#datasource-selector");
        $selector.html(selectorContent);

    } else {
        //its being re-enabled so need to re populate all the datasources
        populateDatasourceSelector();

    }

    populateSorter();
    reinitializeScrollpanes();

}

function populateDatasourceSelector() {
    // populate checkboxes
    var selectorContent = "";
    for (var i = 0; i < datasourceNames.length; i++) {
        var datasource = datasourceNames[i];

        selectorContent = selectorContent +
            "<li style=\"margin-bottom: 1px;\">" +
            "<input type=\"checkbox\" name=\"" + datasource + "\" value=\"" + datasource + "\" >" +
            "<a id=\"description-" + datasource + "\" onclick=\"showDesc(this)\" onmouseout=\"hideDesc(this)\" style=\"cursor:help;\">" +
            datasource +
            "</a></li>";

    }
    var $selector = $("#datasource-selector");
    $selector.html(selectorContent);
    $selector.find("input").change(function() {
        populateSorter();
        reinitializeScrollpanes();
    });
}

function showDesc(source){
    var sourceName;
    if (source.value != undefined && source.value != 0){
        sourceName = source.value.toString();
    } else {
        sourceName = source.textContent.toString();
    }

    $("#description-" + sourceName).tooltip({ items: "#description-" + sourceName, content: getSourceDescription(sourceName), show:null,  hide: {effect: ""}, //fadeout
        close: function(event, ui){
            ui.tooltip.hover(
                function(){
                    $(this).stop(true).fadeTo(400, 1);
                },
                function(){
                    $(this).fadeOut("400", function(){
                        $(this).remove();
                    })
                }
            );
        }});
    $("#description-" + sourceName).tooltip("enable"); // this line added
    $("#description-" + sourceName).tooltip("open");
}


function hideDesc(source){
    var sourceName;
    if (source.value != undefined && source.value != 0){
        sourceName = source.value.toString();
    } else {
        sourceName = source.textContent.toString();
    }

    if ($("#description-" + sourceName).data('ui-tooltip')){
        $("#description-" + sourceName).tooltip("disable");
    }

}


function populateSorter() {

    var checked = false;
    $("#select-none-datasources").find("input:checked").each(function() {
        if($(this).prop("value") == "Select None"){
            checked = true;
        }
    });

    // retrieve checked selector items, if Select None not checked
    var datasourceNames = [];
    var $selector = $("#datasource-selector");
    if (!checked) {
        $selector.find("input:checked").each(function () {
            datasourceNames.push($(this).prop("value"));
        });
    }

    // if nothing checked, and Select None not checked use all elements
    if (datasourceNames.length == 0 && !checked) {
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

function getRealName(name){
    var realName;

    if (name == "ExpressionAtlas"){
        realName = "atlas";
    } else if (name == "GWAS"){
        realName = "gwas";
    } else if (name == "OpenTargets"){
        realName = "cttv";
    } else if (name == "UniProt"){
        realName = "uniprot";
    } else if (name == "EBiSC"){
        realName = "ebisc";
    } else if (name == "ClinVar"){
        realName = "eva-clinvar";
    } else if (name == "CellularPhenoTypes"){
        realName = "sysmicro";
    } else if (name == "CBI"){
        realName = "cbi";
    } else if (name == "ClinVarXRefs") {
        realName = "clinvar-xrefs";
    } else {
        realName = name;
    }

    return realName;
}

function getRequiredSourcesParam() {
    var selected = [];

    $("#select-none-datasources").find("input:checked").each(function() {
        if($(this).prop("value") == "Select None"){
            selected.push($(this).prop("value"));;
        }
    });

    // get child, selected input elements of 'datasource-selector'
    $("#datasource-selector").find("input:checked").each(function() {
        selected.push($(this).prop("value"));
    });

    var required = "";
    if (selected.length > 0) {
        required = "required:[";
        for (var i = 0; i < selected.length - 1; i++) {
            required = required + getRealName(selected[i]) + ",";
        }
        required = required + getRealName(selected[i]) + "]";
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
            preferred = preferred + getRealName(sorted[i]) + ",";
        }
        preferred = preferred + getRealName(sorted[i]) + "]";
    }
    return preferred;
}

function getOntologySourcesParam() {
    var selected = [];

    $("#select-none-ontologies").find("input:checked").each(function() {
        if($(this).prop("value") == "Select None"){
            selected.push($(this).prop("value"));;
        }
    });

    // get child, selected input elements of 'selected-ontologies'
    $("#selected-ontologies").find("input:checked").each(function () {
        selected.push($(this).prop("value"));
    });

    var required = "";
    if (selected.length > 0) {
        required = "ontologies:[";
        for (var i = 0; i < selected.length - 1; i++) {
            required = required + selected[i] + ",";
        }
        required = required + selected[i] + "]";
    }
    return required;
}

function doSearch(json) {
    var payload = JSON.stringify(json);
    var requiredSources = getRequiredSourcesParam();
    var preferredSources = getPreferredSourcesParam();
    var ontologySources = getOntologySourcesParam();
    var url = 'v2/api/services/map';
    if (requiredSources || preferredSources || ontologySources) {
        url = url + '?filter=';
        if (requiredSources) {
            url = url + requiredSources;
        }
        if (preferredSources) {
            url = url + preferredSources;
        }
        if (ontologySources) {
            url = url + ontologySources;
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
        var prop_high = 0;
        var prop_good = 0;
        var prop_medium = 0;
        var prop_low = 0;
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

                prop_high++;
                aux_type = result[0];
                aux_value = result[1];
            }
            else if (result[4] == "Good" || result[4] == "Medium" || result[4] == "Low") {

                row = "<tr class='curation'>";
                if (result[0] != aux_type || result[1] != aux_value) {
                    if (result[4] == "Good") {
                        prop_good++;
                    } else if (result[4] == "Medium"){
                        prop_medium++;
                    } else if (result[4] == "Low"){
                        prop_low++;
                    }
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
                if (loadedOntologyURIs.indexOf(result[7]) > -1) {
                    //found in OLS
                    // no comma separation in results from OLS, linkify entire field
                    row = row + "<td>" + linkify("//www.ebi.ac.uk/ols/search?exact=true&q=" + result[6] + result[5] + "&ontology=" + uriNameMap[result[7]].toLowerCase(), result[5]) + "</td>";

                } else {
                    // multiple mappings will be comma separated
                    if (result[5].indexOf(", ") == -1) {
                        // no comma separation, linkify entire field
                        row = row + "<td>" + linkify("//www.ebi.ac.uk/ols/search?exact=true&q=" + result[6] + result[5], result[5]) + "</td>";
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
                                links += linkify("//www.ebi.ac.uk/ols/search?exact=true&q=" + ontologyURI + termID, termID) + ",<br />";
                            }
                            links += linkify("//www.ebi.ac.uk/ols/search?exact=true&q=" + ontologyURIs[l] + termIDs[l] , termIDs[l]);

                            row = row + "<td>" + links + "</td>";
                        }
                    }
                }
            }
            else {
                row = row + "<td>" + result[5] + "</td>";
            }
            if (result[7] != "N/A") {
                //var ontoSourceArray = [];
                //var ontoName = "";
                //if (result[7].indexOf("/") > -1 ){ // if the source has a dash in it (e.g. www.ebi.ac.uk/efo) get the name at the end (e.g. efo)
                //    ontoSourceArray = result[7].split("/");
                //    ontoName = ontoSourceArray[(ontoSourceArray.length - 1)];
                //}
                //if (ontoName.indexOf(".") > 1){ // if the result has a dot in it (e.g. efo.owl) get the name (e.g. efo)
                //    var noDot = ontoName.split(".");
                //    ontoName = noDot[0];
                //}
                var href;
                if (loadedOntologyURIs.indexOf(result[7]) > -1){
                    row = row + "<td><a href='" + "//www.ebi.ac.uk/ols/ontologies/" + uriNameMap[result[7]] + "' target='_blank'>" +
                        "<img src='images/ols-logo.jpg' " +
                        "alt='" + uriNameMap[result[7]] + "' style='height: 20px;'/> " + uriNameMap[result[7]] + "</a></td>";
                }
                else if (result[7] == "https://www.ebi.ac.uk/gxa") {
                    href = result[7];
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='//www.ebi.ac.uk/gxa/resources/images/ExpressionAtlas_logo_web.png' " +
                            "alt='Expression Atlas' style='height: 22px;'/> Expression Atlas</a></td>";
                }
                //TODO: change the loader to get the ebi gwas website
                else if (result[7] == "http://www.genome.gov/gwastudies/") {
                    href = "//www.ebi.ac.uk/gwas";
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='images/nhgri.png' " +
                            "alt='GWAS' style='height: 22px;'/> GWAS</a></td>";
                }
                else if (result[7] == "https://www.ebi.ac.uk/fg/sym") {
                    href = result[7];
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='images/CelPh_logo.gif' " +
                            "alt='CellularPhenoTypes' style='height: 22px;'/> CellularPhenoTypes</a></td>";
                }
                else if (result[7] == "https://www.ebi.ac.uk/metabolights") {
                    href = result[7];
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='images/MetaboLightsLogo.png' " +
                            "alt='Metabolights' style='height: 22px;'/> Metabolights</a></td>";
                }
                else if (result[7] == "https://github.com/EBISPOT/EFO-UKB-mappings") {
                    href = result[7];
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='images/ukbiobank.jpg' " +
                            "alt='UKBioBank' style='height: 22px;'/> UK Biobank</a></td>";
                }
                else if (result[7] == "https://www.ebi.ac.uk/uniprot") {
                    row = row + "<td><a href='//www.ebi.ac.uk/uniprot' target='_blank'>" +
                            "<img src='images/uniprot_logo.gif' " +
                            "alt='UniProt' style='height: 20px;'/> UniProt</a></td>";
                }
                else if (result[7] == "https://www.targetvalidation.org"){
                    href = result[7];
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                        "<img src='images/ot_logo_webheader.svg' " +
                        "alt='OpenTargets' style='height: 20px;'/> Open Targets</a></td>";
                }
                else if (result[7] == "https://www.ebi.ac.uk/eva"){
                    href = result[7];
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                        "<img src='images/eva_logo.png' " +
                        "alt='ClinVar' style='height: 20px;'/> EVA ClinVar</a></td>";
                }
                else if (result[7] == "https://cells.ebisc.org/"){
                    href = result[7];
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                        "<img src='images/EBiSC-logo.png' " +
                        "alt='EBiSC' style='height: 20px;'/> EBiSC</a></td>";
                } else if (result[7] == "https://www.ebi.ac.uk/biosamples"){
                    href = result[7];
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                        "<img src='images/cbi_icon.png' " +
                        "alt='CBI' style='height: 20px;'/> CBI </a></td>";
                }  else if (result[7] == "https://www.ncbi.nlm.nih.gov/clinvar"){
                    href = result[7];
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                        "<img src='images/clinvarxrefs-logo.png' " +
                        "alt='ClinVar xRefs' style='height: 20px;'/> ClinVar xRefs </a></td>";
                } else {
                    var sourceName = uriNameMap[result[7]];
                    row = row + "<td><a href='" + result[7] + "' target='_blank'>" +
                        sourceName + " </a></td>";
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

        prop_total = prop_high + prop_good + + prop_medium + prop_low + prop_unmapped;
        if (prop_total > 0) {
            var $zoomaStats = $("#zooma-stats");

            var statsContent = "Stats: &nbsp;&nbsp;&nbsp;&nbsp; " + prop_total + " properties " +
                    "&nbsp;&nbsp;&nbsp;&nbsp;" +
                    prop_high + " high" +
                    "&nbsp;&nbsp;&nbsp;&nbsp; " +
                    prop_good + " good" +
                    "&nbsp;&nbsp;&nbsp;&nbsp; " +
                    prop_medium + " medium" +
                    "&nbsp;&nbsp;&nbsp;&nbsp; " +
                    prop_low + " low" +
                    "&nbsp;&nbsp;&nbsp;&nbsp; " +
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