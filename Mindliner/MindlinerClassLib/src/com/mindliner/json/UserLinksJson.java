/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.json;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dominic Plangger
 */
public class UserLinksJson {

    public UserLinksJson(String name) {
        this.name = name;
        this.links = new ArrayList<>();
    }
    
    public void addLink(String target, long count) {
        links.add(new Link(target, count));
    }
    
    public final String name;
    private final List<Link> links;
    
    private static class Link {
        public Link(String target, long count) {
            this.target = target;
            this.count = count;
        }
        private final String target;
        private final long count; 
    }
}
