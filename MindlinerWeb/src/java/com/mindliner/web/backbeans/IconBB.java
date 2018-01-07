/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.mindliner.analysis.MlClassHandler.MindlinerObjectType;
import static com.mindliner.analysis.MlClassHandler.MindlinerObjectType.Contact;
import static com.mindliner.analysis.MlClassHandler.MindlinerObjectType.Image;
import static com.mindliner.analysis.MlClassHandler.MindlinerObjectType.Knowlet;
import static com.mindliner.analysis.MlClassHandler.MindlinerObjectType.Task;
import com.mindliner.categories.MlsEventType;
import com.mindliner.contentfilter.Completable;
import com.mindliner.entities.MlsContainer;
import com.mindliner.entities.MlsContainerMap;
import com.mindliner.entities.MlsImage;
import com.mindliner.entities.MlsNews;
import com.mindliner.entities.mlsContact;
import com.mindliner.entities.mlsKnowlet;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsObjectCollection;
import com.mindliner.entities.mlsTask;
import javax.faces.bean.RequestScoped;
import javax.inject.Named;

/**
 *
 * @author Marius Messerli
 */
@Named(value = "iconBB")
@RequestScoped
public class IconBB {

    /**
     * Creates a new instance of IconBB
     */
    public IconBB() {
    }

    /**
     * Returns a static URL to an icon representing the class of the specified
     * object.
     *
     * @param object The object for which we need a class icon
     * @return The URL String that points to the class icon for the specified object
     */
    public String getClassIconUrl(mlsObject object) {

        if (object instanceof mlsTask) {
            return getTypeIconUrl(MindlinerObjectType.Task);
        } else if (object instanceof mlsKnowlet) {
            return getTypeIconUrl(MindlinerObjectType.Knowlet);
        } else if (object instanceof mlsObjectCollection) {
            return getTypeIconUrl(MindlinerObjectType.Collection);
        } else if (object instanceof mlsContact) {
            return getTypeIconUrl(MindlinerObjectType.Contact);
        } else if (object instanceof MlsNews) {
            return getTypeIconUrl(MindlinerObjectType.News);
        } else if (object instanceof MlsImage) {
            return getTypeIconUrl(MindlinerObjectType.Image);
        } else if (object instanceof MlsContainer) {
            return getTypeIconUrl(MindlinerObjectType.Container);
        } else if (object instanceof MlsContainerMap) {
            return getTypeIconUrl(MindlinerObjectType.Map);
        }
        return "";
    }

    public String getTypeIconUrl(MindlinerObjectType type) {
        switch (type) {
            case Knowlet:
                return "/resources/images/icons/32/information2.png";

            case Task:
                return "/resources/images/icons/32/preferences.png";
            case Collection:
                return "/resources/images/icons/32/folder2_blue.png";

            case Contact:
                return "/resources/images/icons/32/users2.png";

            case News:
                return "/resources/images/icons/32/flash_red.png";

            case Image:
                return "/resources/images/icons/32/photo_landscape.png";
                
            case Container:
                return "/resources/images/icons/32/layout.png";
                
            case Map:
                return "/resources/images/icons/32/chart_dot.png";

            default:
                throw new AssertionError();
        }
    }

    public String getPrivacyIconUrl(mlsObject object) {
        if (object.getPrivateAccess()) {
            return "/resources/images/icons/32/lock.png";
        } else {
            return "/resources/images/icons/32/lock_open.png";
        }
    }

    public String getCompletionIconUrl(mlsObject object) {
        if (object instanceof Completable) {
            Completable c = (Completable) object;
            if (c.isCompleted()) {
                return "/resources/images/icons/32/checkbox.png";
            } else {
                return "/resources/images/icons/32/checkbox_unchecked.png";
            }
        }
        return "";
    }
    
    public String getEventIcon(MlsEventType.EventType type) {
        switch(type) {
            case ObjectCreated:
                return "bulb.png";
            case ObjectDeleted:
                return "trash.png";
            case ObjectUpdated:
                return "pen.png";
            default:
                return "table.png";
        }
    }
}
