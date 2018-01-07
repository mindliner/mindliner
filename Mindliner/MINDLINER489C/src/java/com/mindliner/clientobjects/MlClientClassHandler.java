/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.analysis.MlClassHandler;
import static com.mindliner.analysis.MlClassHandler.MindlinerObjectType.Any;
import static com.mindliner.analysis.MlClassHandler.MindlinerObjectType.Collection;
import static com.mindliner.analysis.MlClassHandler.MindlinerObjectType.Contact;
import static com.mindliner.analysis.MlClassHandler.MindlinerObjectType.Knowlet;
import static com.mindliner.analysis.MlClassHandler.MindlinerObjectType.Image;
import static com.mindliner.analysis.MlClassHandler.MindlinerObjectType.Task;
import com.mindliner.entities.*;

/**
 * This class matches the client classes to corresponding server classes.
 *
 * @author Marius Messerli
 */
public class MlClientClassHandler extends MlClassHandler {

    /**
     * If a client class is specified the function returns the corresponding
     * server class. If a server class is specified it is simply returned
     * unchanged. Example: for input mlcKnowlet.class the return value is
     * mlsKnowlet.class
     *
     * @param clazz The class for which the corresponding server class is
     * requested.
     * @return
     */
    public static Class getMatchingServerClass(Class clazz) {
        Class returnClass = null;
        if (clazz == null) {
            return null;
        }

        if (clazz == mlcNews.class) {
            returnClass = MlsNews.class;
        } else if (clazz == mlcContact.class) {
            returnClass = mlsContact.class;
        } else if (clazz == mlcKnowlet.class) {
            returnClass = mlsKnowlet.class;
        } else if (clazz == mlcObject.class) {
            returnClass = mlsObject.class;
        } else if (clazz == mlcObjectCollection.class) {
            returnClass = mlsObjectCollection.class;
        } else if (clazz == mlcTask.class) {
            returnClass = mlsTask.class;
        } else if (clazz == MlcImage.class) {
            return MlsImage.class;
        } else if (clazz == MlcContainer.class) {
            return MlsContainer.class;
        } else if (clazz == MlcContainerMap.class) {
            return MlsContainerMap.class;
        }
        if (returnClass != null) {
            return returnClass;
        }
        throw new IllegalArgumentException("No matching server class found for " + clazz.getName());
    }


    public static Class getClassByType(MindlinerObjectType type) {
        switch (type) {
            case Any:
                return mlcObject.class;
            case Collection:
                return mlcObjectCollection.class;
            case Contact:
                return mlcContact.class;
            case Knowlet:
                return mlcKnowlet.class;
            case Task:
                return mlcTask.class;
            case Image:
                return MlcImage.class;
            case Container:
                return MlcContainer.class;
            case Map:
                return MlcContainerMap.class;
            default:
                throw new AssertionError();
        }
    }

    public static MindlinerObjectType getTypeByClass(Class c) {
        if (c == mlcNews.class) {
            return MindlinerObjectType.News;
        } else if (c == mlcObjectCollection.class) {
            return MindlinerObjectType.Collection;
        } else if (c == mlcContact.class) {
            return MindlinerObjectType.Contact;
        } else if (c == mlcKnowlet.class) {
            return MindlinerObjectType.Knowlet;
        } else if (c == mlcTask.class) {
            return MindlinerObjectType.Task;
        } else if (c == MlcImage.class) {
            return MindlinerObjectType.Image;
        } else if (c == MlcContainer.class) {
            return MindlinerObjectType.Container;
        } else if (c == MlcContainerMap.class) {
            return MindlinerObjectType.Map;
        }
        throw new IllegalArgumentException("No type for specified class: " + c.getName());
    }
    
    public static String getNameByType(MindlinerObjectType type) {
        switch (type) {
            case News:
                return "News";
            case Any:
                return "Any";
            case Collection:
                return "Collection";
            case Contact:
                return "Contact";
            case Knowlet:
                return "Knowlet";
            case Task:
                return "Task";
            case Image:
                return "Image";
            case Container:
                return "Container";
            case Map:
                return "Map";
            default:
                throw new AssertionError();
        }
    }
}
