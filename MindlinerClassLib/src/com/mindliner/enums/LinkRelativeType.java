/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.enums;

/**
 *
 * @author Dominic Plangger
 */
public enum LinkRelativeType {
    
    OBJECT, // hard link between two Mindliner objects
    ICON_OBJECT, // hard link between an Mindliner object and an icon
    WEBSITE, // seems to be unused at the minute
    CONTAINER_MAP // links containers and objects to templates and objects to containers. Are shown in Mindmap.
}
