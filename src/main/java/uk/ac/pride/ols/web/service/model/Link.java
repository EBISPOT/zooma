package uk.ac.pride.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 01/03/2016
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {

    @JsonProperty("first")
    Href first;

    @JsonProperty("self")
    Href self;

    @JsonProperty("next")
    Href next;

    @JsonProperty("last")
    Href last;

    @JsonProperty("prev")
    Href prev;

    public Href getFirst() {
        return first;
    }

    public void setFirst(Href first) {
        this.first = first;
    }

    public Href getSelf() {
        return self;
    }

    public void setSelf(Href self) {
        this.self = self;
    }

    public Href getNext() {
        return next;
    }

    public void setNext(Href next) {
        this.next = next;
    }

    public Href getLast() {
        return last;
    }

    public void setLast(Href last) {
        this.last = last;
    }

    public Href getPrev() {
        return prev;
    }

    public void setPrev(Href prev) {
        this.prev = prev;
    }
}
