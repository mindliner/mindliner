/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.converters;

import javax.inject.Named;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * This converter limits the length of the description text to a default value.
 *
 * @author Marius Messerli
 */
@Named(value = "descriptionTruncationConverter")
public class DescriptionTruncationConverter implements Converter {

    private static final int MAX_LENGTH = 400;

    /**
     * Creates a new instance of DescriptionTruncationConverter
     */
    public DescriptionTruncationConverter() {
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return value;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null && value instanceof String) {
            String s = (String) value;
            return s.length() > MAX_LENGTH ? s.substring(0, MAX_LENGTH).concat("...") : s;
        }
        return "";
    }

}
