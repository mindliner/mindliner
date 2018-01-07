/*
 * RecordLoader.java
 * 
 * Created on 05.06.2007, 20:56:45
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.mindliner.contentfilter.loaders;

import com.mindliner.contentfilter.BaseFilter;
import javax.persistence.EntityManager;

/**
 * All classes that are able to load elements into an existing filter
 * must implement this interface.
 * @author messerli
 */
public interface RecordLoader {
    
    public void load(BaseFilter filter, EntityManager em);

}
