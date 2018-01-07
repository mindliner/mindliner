/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.cache;

/**
 * All agents of the cache system must implement this interface.
 * 
 * @author Marius Messerli
 */
public interface CacheAgent {
    
    void initialize() throws MlCacheException;
        
    /**
     * Appends all the cache item into the specified object output stream.
     * @throws com.mindliner.cache.MlCacheException if anything goes wrong - most likely I/O
     */
    void storeCache() throws MlCacheException;
    
    /**
     * Curates the cache: deletes outdated versions of objects and links. 
     * This function is available in online mode, only.
     * @param force If true then maintenance will be performed on all parts irrespective of when the last maintenance run took place. If false then a lighter maintenance will be performed.
     */
    void performOnlineCacheMaintenance(boolean force) throws MlCacheException;
    
}
