/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.converters;

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
public class MlPriorityConverter implements Converter, Serializable {

    @EJB
    private CategoryManagerRemote categoryManagerBean;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        for (mlsPriority p : categoryManagerBean.getAllPriorities()) {
            if (p.getName().equals(value)) {
                return p;
            }
        }
        throw new IllegalStateException("No priority found with name " + value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((mlsPriority)value).getName();
    }
}
