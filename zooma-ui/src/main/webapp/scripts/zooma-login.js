var rootContextPath;

$(document).ready(init());

function init() {
    if (rootContextPath == null) {
        rootContextPath = "./";
    }

    // enable log in/out
    checkLoginStatus();
    checkLoginReferrer();
}

function checkLoginStatus() {
    $.getJSON(rootContextPath + 'secure/authentication/user-summary', function(data) {
        if (data.isAuthenticated == "true") {
            $("#zooma-login").html("<a href=\"" + rootContextPath + "secure/logout.html\" " +
                                           "class=\"icon icon-functional\" data-icon=\"l\">" +
                                           "Not " + data.firstName + "? Logout" +
                                           "</a>");
        }
        else {
            $("#zooma-login").html("<a href=\"" + rootContextPath + "secure/login.html\" " +
                                           "class=\"icon icon-functional\" data-icon=\"l\">" +
                                           "Login" +
                                           "</a>");
        }
    });
}

function checkLoginReferrer() {
    // get cookie
    var cookieName = "zooma.login.referredFrom";
    var userCameFrom = getCookie(cookieName);
    // and delete cookie
    deleteCookie(cookieName);
    if (userCameFrom && userCameFrom.length > 0) {
        var sendBackTo = getRedirectURL(userCameFrom);
        $("head").append("<meta http-equiv=\"refresh\" content=\"3; URL=" + sendBackTo + "\">");
    }
}

function getRedirectURL(referredFrom) {
    var fromURI = URI(referredFrom);
    var currentURI = URI(window.location);
    if (fromURI.domain() == currentURI.domain()) {
        return fromURI.path();
    }
    else {
        return referredFrom;
    }
}

function getCookie(cookieName) {
    var name = cookieName + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++)
    {
        var c = ca[i].trim();
        if (c.indexOf(name)==0) return c.substring(name.length,c.length);
    }
    return "";
}

function deleteCookie(cookieName) {
    document.cookie = cookieName + "=; expires=Thu, 01 Jan 1970 00:00:00 GMT";
}
