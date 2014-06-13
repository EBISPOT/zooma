package uk.ac.ebi.fgpt.zooma.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.fgpt.zooma.exception.UnrecognisedUserOrTypeException;
import uk.ac.ebi.fgpt.zooma.model.ZoomaUser;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUsers;

import java.util.HashMap;
import java.util.Map;

/**
 * A controller stereotype that allows the generation or retrieval of a ZOOMA REST API key. Authenticated users can use
 * this controller to obtain their key from the ZOOMA website.
 *
 * @author Tony Burdett
 * @date 14/10/13
 */
@Controller
@RequestMapping("/authentication")
public class ZoomaApiKeyController {
    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @RequestMapping(value = "/whoami", method = RequestMethod.GET)
    public @ResponseBody String echoUserName() {
        ZoomaUser user = ZoomaUsers.getCurrentUser(); // checks for null, inserts 'nobody'
        if (user != null) {
            return user.getFullName();
        }
        else {
            return "nobody";
        }
    }

    @RequestMapping(value = "/key-request", method = RequestMethod.GET)
    public @ResponseBody String getApiKey() {
        ZoomaUser user = ZoomaUsers.getCurrentUser(); // checks for null, throws exception
        if (user != null) {
            return user.getApiKey();
        }
        else {
            throw new UnrecognisedUserOrTypeException("You are currently anonymous - No API key available");
        }
    }

    @RequestMapping(value = "/user-summary", method = RequestMethod.GET)
    public @ResponseBody Map<String, String> getUserSummary() {
        Map<String, String> userSummary = new HashMap<>();
        ZoomaUser user = ZoomaUsers.getCurrentUser(); // checks for null, reports authenticated = false
        if (user != null) {
            userSummary.put("isAuthenticated", "true");
            userSummary.put("firstName", user.getFirstname());
            userSummary.put("fullName", user.getFullName());
            userSummary.put("apiKey", user.getApiKey());
            userSummary.put("email", user.getEmail());
        }
        else {
            userSummary.put("isAuthenticated", "false");
        }
        return userSummary;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(Exception.class)
    public @ResponseBody String handleException(UnrecognisedUserOrTypeException e) {
        return e.getMessage();
    }
}
