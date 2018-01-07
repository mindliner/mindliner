/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis;

/**
 * This class returns single character abbreviations for server objects. Note: A
 * similar class exists for client objects and must not be confused with this.
 *
 * @author Marius Messerli
 */
public class MlClassHandler {
    
    public static enum MindlinerObjectType{
        Contact,
        Knowlet,
        Collection,
        Task,
        Image,
        Container,
        Map,
        Any, 
        News
    }

    public static final String ActionItem_One_Character_Abbreviation = "A";
    public static final String Contact_One_Character_Abbreviation = "P";
    public static final String Knowlet_One_Character_Abbreviation = "K";
    public static final String ObjectCollection_One_Character_Abbreviation = "C";
    public static final String Task_One_Character_Abbreviation = "T";
    public static final String Cell_One_Character_Abbreviation = "N";
    public static final String Image_One_Character_Abbreviation = "I";
    public static final String Map_One_Character_Abbreviation = "W";
    
    public static String getClassNameOnly(String pathAndClassName) {
        if (pathAndClassName.contains(".")) {
            return pathAndClassName.substring(pathAndClassName.lastIndexOf('.') + 1);
        } else {
            return pathAndClassName;
        }
    }

    public static MindlinerObjectType getTypeByCharacterSymbol(String classCharacter){
        String character = classCharacter.toLowerCase();
        if (character == null || character.isEmpty()) {
            return MindlinerObjectType.Any;
        }
        if (character.equals(Contact_One_Character_Abbreviation.toLowerCase())) {
            return MindlinerObjectType.Contact;
        }
        if (character.equals(Knowlet_One_Character_Abbreviation.toLowerCase())) {
            return MindlinerObjectType.Knowlet;
        }
        if (character.equals(ObjectCollection_One_Character_Abbreviation.toLowerCase())) {
            return MindlinerObjectType.Collection;
        }
        if (character.equals(Task_One_Character_Abbreviation.toLowerCase())) {
            return MindlinerObjectType.Task;
        }
        if (character.equals(Image_One_Character_Abbreviation.toLowerCase())){
            return MindlinerObjectType.Image;
        }
        if (character.equals(Map_One_Character_Abbreviation.toLowerCase())){
            return MindlinerObjectType.Map;
        }
        
        return MindlinerObjectType.Any;        
    }
}
