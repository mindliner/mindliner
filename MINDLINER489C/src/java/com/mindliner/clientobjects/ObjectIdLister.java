/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class converts a list of objects into a list of object IDs.
 *
 * @author Marius Messerli
 */
public class ObjectIdLister {

    public static List<Integer> getIdList(List<mlcObject> items) {
        List<Integer> ilist = new ArrayList<>();
        if (items != null) {
            items.stream().forEach((o) -> {
                ilist.add(o.getId());
            });
        }
        return ilist;
    }

    public static int[] getIds(List<mlcObject> items) {
        int[] inta = new int[items.size()];
        Iterator it = items.iterator();
        for (int i = 0; it.hasNext(); i++) {
            mlcObject m = (mlcObject) it.next();
            inta[i] = m.getId();
        }
        return inta;
    }
}
