package com.mindliner.common;

import com.mindliner.entities.mlsObject;
import java.util.ArrayList;
import java.util.List;

/**
 * This class specifies the parameters of automatically breaking down the
 * children of a collection into subcollections.
 *
 * @author Marius Messerli<marius@mindliner.com>
 */
public class SubCollectionFilter {

    private final int maxChildrenPerCollection;
    private final List<mlsObject> input;
    private int listPosition = 0;

    public SubCollectionFilter(List<mlsObject> input, int maxAllowedChildrenPerSubCollection) {
        this.maxChildrenPerCollection = maxAllowedChildrenPerSubCollection;
        this.input = input;
    }

    public int getMaxAllowedChildrenPerSubCollection() {
        return maxChildrenPerCollection;
    }

    public boolean hasMore() {
        return (input.size() - listPosition) > 0;
    }

    public List<mlsObject> next() {
        List<mlsObject> subCollection = new ArrayList<>();
        int start = listPosition;
        for (int i = listPosition; i < input.size() && i < start + maxChildrenPerCollection; i++) {
            subCollection.add(input.get(i));
            listPosition++;
        }
        return subCollection;
    }
}
