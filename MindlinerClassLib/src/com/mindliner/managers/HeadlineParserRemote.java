/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.mlModifiedException;
import javax.ejb.Remote;

/**
 * This bean edits the headline that may contains tags for various attributes. Some
 * of the tags require database searches which is why this call is implemented
 * on the server side.
 *
 * @author Marius Messerli
 */
@Remote
public interface HeadlineParserRemote {

    /**
     * This call parses the specified String, interprets any tags and applies them to the object.
     * @param objectId The object who's headline and attributes are to be updated
     * @param headline The new headline, possibly with tags that are interpreted and stripped off
     * @return The version of the object after the update
     * @throws mlModifiedException If the object was modified 
     * @throws NonExistingObjectException If no object with the specified id exists on the server.
     */
    public int updateHeadline(int objectId, String headline) throws mlModifiedException, NonExistingObjectException;
}
