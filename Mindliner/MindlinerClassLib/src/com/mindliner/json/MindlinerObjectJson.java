package com.mindliner.json;

import com.mindliner.entities.mlsObject;

/**
 * Interface for building json object relatives trees
 * @author Ming
 */
public interface MindlinerObjectJson {
    
    public void addNode(mlsObject parent, mlsObject object);
}
