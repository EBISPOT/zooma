function init() {
    $(document).ready(function() {
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

        // render table contents if there are results in session
        getResults();

        $("#zooma-explorebox").zooma({'api_base_url': ''});
    });
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
    $("#zooma-textarea").val("Lung adenocarcinoma\nCD4-positive\ncooked broccoli\tcompound\nadipose tisue\n" +
                                     "cerebelum\nPopulus trichocarpa alone\nspinal cord, lower\n120 days\tage\n" +
                                     "2.5-3.5 days\ttime\n2.5 to 3.5 days\tage\n3-4 days\ttime\n10.5 days\tage\n" +
                                     "doxycycline 130 nanomolar\tcompound\n" +
                                     "0.333 millimolar salicylic acid\tgrowth condition\n" +
                                     "nifedipine 0.025 micromolar\tcompound\n" +
                                     "hepatocellular carcinoma (HCC)\torganism_part\n" +
                                     "pancreatic ductal adenocarcinoma (PDAC)\torganism_part\n" +
                                     "peripheral ganglion\torganism part\nleft tibia\torganism part");
}

function annotate(content) {
    resetSession(function(response) {
        log(response);
        doSearch(jsonifyTextArea(content));
    });
}

function annotateOLD(content) {
    resetSession(function(response) {
        log(response);
        doSearchOLD(jsonifyTextArea(content));
    });
}

function clearAll() {
    resetSession(function(response) {
        log(response);
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

function doSearch(json) {
    var payload = JSON.stringify(json);
    $.ajax({
               type: 'POST',
               url: 'v2/api/services/map',
               contentType: 'application/json',
               data: payload,
               beforeSend: function() {
                   $("#progressbar").progressbar({value: false}).show();
               },
               success: function(response) {
                   log(response);
                   setTimeout(checkStatus, 100);
               },
               error: function(request, status, error) {
                   alert(error + ": (" + request.responseText + ")");
               }
           });
}

function doSearchOLD(json) {
    var payload = JSON.stringify(json);
    $.ajax({
               type: 'POST',
               url: 'v2/api/services/map/OLD',
               contentType: 'application/json',
               data: payload,
               beforeSend: function() {
                   $("#progressbar").progressbar({value: false}).show();
               },
               success: function(response) {
                   log(response);
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

    // render new payload
    var payload = data.data;
    if ($.isEmptyObject(payload) == false) {
        var prop_automatic = 0;
        var prop_curation = 0;
        var prop_unmapped = 0;
        var prop_total = 0;

        var aux_type = "";
        var aux_value = "";

        var tableContent = "";
        var spanTo = -1;
        for (var i = 0; i < payload.length; i++) {
            var result = payload[i];
            var row;
            var rowspan = 1;
            if (result[3] == "Automatic") {
                row = "<tr class='automatic'>";

                prop_automatic++;
                aux_type = result[0];
                aux_value = result[1];
            }
            else if (result[3] == "Requires curation") {
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
            row = row + "<td>" + result[3] + "</td>";
            if (result[4] != "N/A") {

                var arrayResult4 = result[4].split(", ");
                var arrayResult5 = result[5].split(", ");

                if (arrayResult4.length == arrayResult5.length) {

                    content = "";

                    for (x = 0; x < arrayResult4.length; x++) {

                        result4 = arrayResult4[x];
                        result5 = arrayResult5[x];

                        if (x == (arrayResult4.length - 1)) {
                            content += linkify(result5 + result4, result4);
                        }
                        else {
                            content += linkify(result5 + result4, result4) + "&nbsp;&nbsp;&nbsp;";
                        }
                    }

                    row = row + "<td>" + content + "</td>";

                }
                else {
                    row = row + "<td>" + linkify(result[5] + result[4], result[4]) + "</td>";
                }

            }
            else {
                row = row + "<td>" + result[4] + "</td>";
            }
            if (result[6] != "N/A") {
                var href;
                if (result[6] == "http://www.ebi.ac.uk/gxa/") {
                    href =
                            "http://www.ebi.ac.uk/gxa/qrs?gprop_0=&gnot_0=&gval_0=%28all+genes%29&fact_1=&fexp_1=UP_DOWN&fmex_1=&fval_1=" +
                                    encodeURIComponent(result[1]) +
                                    "&view=hm&searchMode=simple";
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='http://www.ebi.ac.uk/gxa/images/ExpressionAtlas_logo_web.png' " +
                            "alt='Expression Atlas' style='height: 22px;'/> Expression Atlas</a></td>";
                }
                else if (result[6] == "http://www.ebi.ac.uk/arrayexpress/") {
                    href = "http://www.ebi.ac.uk/arrayexpress/experiments/search.html?query=" +
                            encodeURIComponent(result[1]);
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='http://www.ebi.ac.uk/sites/ebi.ac.uk/files/styles/icon/public/resource/logo/aelogo.jpg' " +
                            "alt='ArrayExpress' style='height: 22px;'/> ArrayExpress</a></td>";
                }
                else if (result[6] == "http://www.ebi.ac.uk/efo/") {
                    href = "http://www.ebi.ac.uk/efo/search?query=" +
                            encodeURIComponent(result[2]) +
                            "&submitSearch=Search";
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='http://www.ebi.ac.uk/sites/ebi.ac.uk/files/styles/thumbnail/public/resource/logo/EFO_logo_0.png' " +
                            "alt='EFO' style='height: 22px;'/> EFO</a></td>";
                }
                else if (result[6] == "http://www.genome.gov/gwastudies/") {
                    href = "http://www.genome.gov/gwastudies/#searchForm";
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='images/nhgri.png' " +
                            "alt='GWAS' style='height: 22px;'/> GWAS</a></td>";
                }
                else if (result[6] == "http://omia.angis.org.au/") {
                    href = result[5] + result[4];
                    row = row + "<td><a href='" + href + "' target='_blank'>" +
                            "<img src='images/omia.png' " +
                            "alt='OMIA' style='height: 22px;'/> OMIA</a></td>";
                }
                else {
                    row = row + "<td>" + result[6] + "</td>";
                }
            }
            else {
                row = row + "<td>" + result[6] + "</td>";
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