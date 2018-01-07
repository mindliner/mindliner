/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.json;

import com.mindliner.entities.mlsObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dominic Plangger
 */
public class DFGRelativesJson implements MindlinerObjectJson {
    
    
    private final List<Node> nodes = new ArrayList<>();
    private final List<Link> links = new ArrayList<>();
    
    @Override
    public void addNode(mlsObject parent, mlsObject obj) {
        for (Node node : nodes) {
            if (node.id == obj.getId()) {
                return;
            }
        }
        Node n = new Node(obj.getHeadline(), obj.getDescription(), obj.getId(), obj.getRating());
        nodes.add(n);
        if(parent != null) {
            addLink(parent.getId(), obj.getId());
        }
    }
    
    private void addLink(int sourceId, int targetId) {
        for (Link link : links) {
            if (link.source == sourceId && link.target == targetId) {
                return;
            }
        }
        Link l = new Link(sourceId, targetId);
        links.add(l);
    }

    
    static class Node {
        private final String headline;
        private final String description;
        private final int id;
        private final double rank;

        public Node(String headline, String description, int id, double rank) {
            this.headline = headline;
            this.description = description;
            this.id = id;
            this.rank = rank;
        }
    }
    
    static class Link {
        private final int source;
        private final int target;

        public Link(int source, int target) {
            this.source = source;
            this.target = target;
        }
    }

}
