/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.MlsNewsType;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.enums.ObjectReviewStatus;
import java.util.Date;
import javax.ejb.Remote;

/**
 *
 * @author Marius Messerli
 */
@Remote
public interface ObjectFactoryRemote {

    public static final String CONFIDENTIALITY_ID = "ConfidentialityId";
    public static final String PRIVATE_ACCESS = "PrivateAccess";
    public static final String LIFETIME = "LifeTime";
    public static final String CREATION_DATE = "CreationDate";
    public static final String TASK_PRIORITY_ID = "TaskPriorityId";
    public static final String COLLECTION_TYPE = "CollectionType";
    public static final String CLASS_NAME = "ClassName";

    /**
     * Creates a new mindliner object for the current user and the current client. Not all the 
     * specified categories will be used as the usage depends on the object class. This needs
     * to be refactored when the new categorysystem becomes active.
     *
     * @param clazz The object class
     * @param clientId The id of the client for which the object is to be created
     * @param confidentialityId The confidentiality for the next object
     * @param creationDate Object will be assigned the specified date, not the actual date of creation (required for correct synch of offline creations)
     * @param priorityId The priority (if applicable)
     * @param relativeId The id of the relative (if any) that is to be linked upon creation. Specify -1 if none is to be linked
     * @param privateAccess Whether or not this object has the private state set
     * @param description The description
     * @param headline The headline
     * @param status The review status (imported or manually reviewed) of the next object
     * @param linkType
     * @param newsType The news category. Ignored unless clazz equals MlsNews.class
     * @return integer array. index zero contains the object id and index one the version number of the created object, index two the version number 
     * of the relative (-1 if no relative specified)
     */
    public int[] create(
            Class clazz,
            int clientId,
            int confidentialityId,
            Date creationDate,
            int priorityId,
            boolean privateAccess,
            int relativeId, 
            String headline, 
            String description,
            ObjectReviewStatus status,
            LinkRelativeType linkType,
            MlsNewsType newsType);
}
