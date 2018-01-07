package com.mindliner.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tusroy Date 06/20/2015
 *
 * Video link - https://youtu.be/ID00PMy0-vE
 *
 * Disjoint sets using path compression and union by rank Supports 3 operations
 * 1) makeSet 2) union 3) findSet
 *
 * For m operations and total n elements time complexity is O(m*f(n)) where f(n)
 * is very slowly growing function. For most cases f(n) <= 4 so effectively
 * total time will be O(m). Proof in Coreman book.
 */
public class DisjointSet {

    private Map<Long, Node> map = new HashMap<>();

    class Node {

        long data;
        Node parent;
        int rank;
    }

    /**
     * Create a set with only one element.
     */
    public void makeSet(long data) {
        Node node = new Node();
        node.data = data;
        node.parent = node;
        node.rank = 0;
        map.put(data, node);
    }

    /**
     * Combines two sets together to one. Does union by rank
     */
    public void union(long data1, long data2) {
        Node node1 = map.get(data1);

        if (node1 == null) {
            // Marius: not quite sure why this happens, seems like the relations produce an object that was not included in the makeset first
            return;
        }
        Node node2 = map.get(data2);
        {
            if (node2 == null) {
                return;
            }
        }

        Node parent1 = findSet(node1);
        Node parent2 = findSet(node2);

        //if they are part of same set do nothing
        if (parent1.data == parent2.data) {
            return;
        }

        //else whoever's rank is higher becomes parent of other
        if (parent1.rank >= parent2.rank) {
            //increment rank only if both sets have same rank
            parent1.rank = (parent1.rank == parent2.rank) ? parent1.rank + 1 : parent1.rank;
            parent2.parent = parent1;
        } else {
            parent1.parent = parent2;
        }
    }

    /**
     * Finds the representative of this set
     */
    public long findSet(long data) {
        return findSet(map.get(data)).data;
    }

    /**
     * Find the representative recursively and does path compression as well.
     */
    private Node findSet(Node node) {
        Node parent = node.parent;
        if (parent == node) {
            return parent;
        }
        node.parent = findSet(node.parent);
        return node.parent;
    }

}
