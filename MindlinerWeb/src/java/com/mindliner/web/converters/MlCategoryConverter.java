/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.converters;

import com.mindliner.categories.mlsMindlinerCategory;
import com.mindliner.categories.mlsPriority;
import com.mindliner.managers.CategoryManagerRemote;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author Marius Messerli
 */
@ManagedBean
@RequestScoped
public class MlCategoryConverter implements Converter, Serializable {

    public static final long serialVersionUID = 5021964;
    @EJB
    private CategoryManagerRemote categoryManager;

    /**
     * Because CDI does not work here (have to read up on it) I pass the string
     * onto the processing BB which does the conversion back to KnowletCategory.
     *
     * @todo Change the category management - if two categories have the same name this converts fails.
     * (for example a Buscat with name "sales" and a KnowletCategory with name "sales" - since I control
     * this for now this is not a problem today.
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        for (mlsPriority p : categoryManager.getAllPriorities()) {
            if (p.getName().equals(value)) {
                return p;
            }
        }
        throw new IllegalStateException("No category found with name " + value); 
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((mlsMindlinerCategory) value).getName();
    }
}
