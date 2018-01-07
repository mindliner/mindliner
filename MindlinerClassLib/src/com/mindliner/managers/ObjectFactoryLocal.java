/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.MlsNewsType;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import com.mindliner.enums.LinkRelativeType;
import javax.ejb.Local;

/**
 *
 * @author Marius Messerli
 */
@Local
public interface ObjectFactoryLocal {

    /**
     * Creates a new Mindliner object with the specified attributes and
     * relations
     *
     * @param clazz The class for the new object
     * @param client The client of the new object
     * @param taskPriority The task priority - ignored if class is not mlsTask
     * @param confidentiality The confidentiality, if it does not exist or if
     * user is not authorized to that level the conf is subtituted with the most
     * stringent level the caller has clearance. Specify -1 to assign max
     * confidentiality for caller.
     * @param relativeId The ID of the first relative of the new object (i.e. a
     * parent in a tree). Specify -1 if none is wanted
     * @param linkType
     * @param newsType The new category. Ignored unless clazz equals
     * MlsNews.class
     * @param headline The initial headline
     * @param description The initial description
     * @return
     */
    public mlsObject createLocal(Class clazz, mlsClient client, mlsPriority taskPriority, mlsConfidentiality confidentiality,
            int relativeId, LinkRelativeType linkType, MlsNewsType newsType,
            String headline, String description);

    /**
     * This is a dedicated call to create news articles, it works the same as the above call otherwise.
     * News articles are delivered through a timer event where there is no current user and thus needs
     * the user on the argument list.
     * 
     * @param client The client for which the news is to be created
     * @param confidentiality The confidentiality level of the news
     * @param headline
     * @param description
     * @param user The subscription user for which the news article is to be created
     * @return
     */
    public mlsObject createNewsRecord(mlsClient client, mlsConfidentiality confidentiality, String headline, String description, mlsUser user);

}
