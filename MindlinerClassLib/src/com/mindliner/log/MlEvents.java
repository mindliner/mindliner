/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.log;

/**
 *
 * @author Marius Messerli
 */
public class MlEvents {
    
    public static enum Events {
        HEADLINE_UPDATE,
        DESCRIPTION_UPDATE,
        OWNER_UPDATE,
        CONFIDENTIALITY_UPDATE,
        PRIVACY_STATE_UPDATE,
        COMPLETION_STATE_UPDATE,
        LIFETIME_UPDATE,
        CATEGORY_UPDATE,
        OBJECT_CREATION,
        LINK_EVENT,
        UNLINK_EVENT
    }
    
}
