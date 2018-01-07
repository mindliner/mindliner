/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A text tag used in headlines to specify non-text attributes and relations.
 * The tag will be removed from the headline after having been processed.
 * 
 * This class hard-codes all the tags that are available in Mindliner.
 *
 * @author Marius Messerli
 */
public class MindlinerStripTag {

    // the character with which all tags start
    public static final String TAG_LEAD_CHARACTER = ".";
    // Mindliner tag objects must have the following string in their description field
    public static final String TAG_OBJECT_HEADLINE_IDENTIFYER = "MindlinerTagObject";

    public static enum TagKey {

        DUEDATE,
        PRIVACY,
        OWNER,
        ID,
        TAG
    }

    private final static Map<TagKey, String> tags = new EnumMap<>(TagKey.class);
    private final static List<String> fullTags = new ArrayList<>();

    static {
        tags.put(TagKey.DUEDATE, "d");
        tags.put(TagKey.PRIVACY, "g");
        tags.put(TagKey.OWNER, "o");
        // this tag links to an existing hash tag but strips the tag from the headline
        tags.put(TagKey.TAG, "t");

        // the following is a strip tag that works like a hash tag except it is removed after processing
        tags.put(TagKey.ID, "i");
        for (TagKey value : TagKey.values()) {
            fullTags.add(getFullTag(value));
        }
    }

    public static List<String> getFullTags() {
        return fullTags;
    }

    public static String getTagCharacter(TagKey key) {
        return tags.get(key);
    }

    /**
     * Returns a string composed of the tag lead character and the tag character
     * @param key The specific key (such as d for due date or p for priority)
     * @return The total tag (such as .d for duedate or .p for priority)
     */
    public static String getFullTag(TagKey key) {
        return TAG_LEAD_CHARACTER.concat(getTagCharacter(key));
    }
    
    public static String getTagObjectHeadline(String baseTag){
        return baseTag.concat(" (").concat(TAG_OBJECT_HEADLINE_IDENTIFYER).concat(")");
    }
}
