/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.converters;

import com.mindliner.analysis.MlsRatingTimerControl;
import com.mindliner.managers.RatingAgentRemote;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * This class converts a mlsClient to and from a string
 *
 * @author Marius Messerli
 */
@ManagedBean
@RequestScoped
public class MlTimerConverter implements Converter, Serializable {

    @EJB
    RatingAgentRemote ratingAgent;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {

        List<MlsRatingTimerControl> timerControls = ratingAgent.getTimerControls();
        for (MlsRatingTimerControl rt : timerControls) {
            if (rt.toString().equals(value)) {
                return rt;
            }
        }
        throw new IllegalStateException("Could not find rating timer control with value " + value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        MlsRatingTimerControl rt = (MlsRatingTimerControl) value;
        return rt.toString();
    }

}
