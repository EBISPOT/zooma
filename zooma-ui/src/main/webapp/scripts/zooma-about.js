$(document).ready(init());

function init() {
    fetchServerInfo();
}

function fetchServerInfo() {
// fetch server info
    $.getJSON('v2/api/server/metadata', function(data) {
        $("#version").html(data.version);
        //$("#build-number").html(data.buildNumber);
        //$("#release-date").html(data.releaseDate);
        var date = new Date(data.startupTime);
        $("#uptime").html(date.toLocaleTimeString() + " on " + date.toLocaleDateString());
    });
}