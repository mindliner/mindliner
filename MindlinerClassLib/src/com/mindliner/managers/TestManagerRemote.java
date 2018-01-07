/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.mlsConfidentiality;
import javax.ejb.Remote;

/**
 *
 * This class has a sporadic test functions and one-time migration operations
 * @author Marius Messerli
 */
@Remote
public interface TestManagerRemote {

    /**
     * Generates a number of dummy knowlets and copies essential attributes from parent.
     * @param recordCount The number of knowlets to be created
     * @param parentId The id of the parent to who all new objects are linked and from whom all essential attributes are copied
     */
    void addDummyContent(int recordCount, int parentId);

    void argumentTransport(mlsConfidentiality conf);

    /**
     * This function tests the data transmission to the server.
     * @param uploadArray The data that is uploaded to the server.
     */
    public void testUploadSpeed(int[] uploadArray);
    
    /**
     * This function tests the download speed by returning an array of the specified size.
     * @param arraySize The size of the int array to be returned.
     * @return An int array of the specified size
     */
    public int[] testDownloadSpeed(int arraySize);
    
    /**
     * A function to call with no upload or download testing connection speed.
     */
    public void testPing();

    void reverseListOrder(int parentId);
       
}
