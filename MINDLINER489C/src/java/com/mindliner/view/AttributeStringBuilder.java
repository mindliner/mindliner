/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.entities.ObjectAttributes;
import com.mindliner.view.TreeLinearizer;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.text.NumberFormatter;
import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.clientobjects.mlcUser;
import java.util.ResourceBundle;

/**
 * Builds a string representing selected object attributes.
 *
 * @author Marius Messerli
 */
public class AttributeStringBuilder {
    
    private static ResourceBundle bundle;
    
    static{
    bundle     = ResourceBundle.getBundle("com/mindliner/resources/GeneralEditor");
    }

    private static String getDateString(Date d) {
        
        final int MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Calendar startOfToday = Calendar.getInstance();
        startOfToday.set(Calendar.MINUTE, 0);
        startOfToday.set(Calendar.SECOND, 0);
        startOfToday.set(Calendar.HOUR_OF_DAY, 0);
        long deltaDays = (d.getTime() - startOfToday.getTime().getTime()) / MILLISECONDS_IN_DAY;
        if (Math.abs(deltaDays) > 7) {
            return sdf.format(d);
        } else if (Math.abs(deltaDays) < 1) {
            sdf = new SimpleDateFormat("HH:mm");
            return sdf.format(d);
        } else if (deltaDays < 0) {
            return Integer.toString((int) -deltaDays) + " " + bundle.getString("AttributeStringBuilder_DaysAgo");
        } else {
            return Integer.toString((int) deltaDays + 1) + " " + bundle.getString("AttributeStringBuilder_DaysFromNow");
        }
    }

    public static String getAttributeDescription(List<ObjectAttributes> attrs, MlMapNode node) {
        StringBuilder sb = new StringBuilder();
        mlcObject object = node.getObject();
        long accumulatedMinutes = 0;

        if (attrs.contains(ObjectAttributes.Owner)) {
            sb.append(object.getOwner().getFirstName());
        }
        if (attrs.contains(ObjectAttributes.ModificationDate)) {
            sb.append(", ");
            sb.append(getDateString(object.getModificationDate()));
        }
        if (attrs.contains(ObjectAttributes.Confidentiality)) {
            sb.append(", ");
            sb.append(object.getConfidentiality().getName());
        }
        if (attrs.contains(ObjectAttributes.Id)) {
            sb.append(", ");
            sb.append("ID:").append(String.format("%,d", object.getId()));
        }
        if (attrs.contains(ObjectAttributes.WorkMinutes)) {
            boolean skip = true;
            int hours;
            int mins;
            String label = "";
            if (object instanceof mlcTask) {
                mlcTask t = (mlcTask) object;
                accumulatedMinutes = t.getAccumulatedActualWorkMinutes();
                if (accumulatedMinutes > 0) {
                    label = bundle.getString("AttributeStringBuilder_WorkTask");
                    skip = false;
                }
            } else if (object instanceof mlcObjectCollection) {
                // to avoid counting the same object twice we keep a record of which we'have accumulated
                List<mlcObject> accumulationObject = new ArrayList<>();
                List<MlMapNode> subBranch = TreeLinearizer.linearizeTree(node, true);
                for (MlMapNode n : subBranch) {
                    if (n.getObject() instanceof mlcTask && !accumulationObject.contains(n.getObject())) {
                        accumulatedMinutes += ((mlcTask) n.getObject()).getAccumulatedActualWorkMinutes();
                        accumulationObject.add(n.getObject());
                    }
                }
                if (accumulatedMinutes > 0) {
                    label = bundle.getString("AttributeStringBuilder_WorkForBranch");
                    skip = false;
                }
            }
            if (!skip) {
                hours = (int) accumulatedMinutes / 60;
                mins = (int) accumulatedMinutes % 60;
                sb.append(", ");
                String text = String.format("%d:%02d", hours, mins);
                sb.append(label).append(text).append("h");
            }
        }
        if (attrs.contains(ObjectAttributes.DataPool)) {
            sb.append(", ");

            sb.append(bundle.getString("AttributeStringBuilder_DataPool")).append(object.getClient().getName());
        }
        if (attrs.contains(ObjectAttributes.DueDate)) {
            if (node.getObject() instanceof mlcTask) {
                mlcTask t = (mlcTask) node.getObject();
                if (t.getDueDate() != null) {
                    sb.append(", ");
                    sb.append(bundle.getString("AttributeStringBuilder_DueDate")).append(getDateString(t.getDueDate()));
                }
            }
        }
        if (attrs.contains(ObjectAttributes.Rating)) {
            if (node.getObject().getRating() > 0D) {
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                NumberFormatter textFormatter = new NumberFormatter(decimalFormat);
                sb.append(", ");
                try {
                    sb.append("R:").append(textFormatter.valueToString(node.getObject().getRating()));
                } catch (ParseException ex) {
                    sb.append("R:").append(node.getObject().getRating());
                    return sb.toString();
                }
            }
        }
        if (object instanceof mlcTask){
            mlcTask t = (mlcTask) object;
            List<mlcUser> currentWorkers = CacheEngineStatic.getCurrentWorkers(t);
            StringBuilder wsb = new StringBuilder();
            for (int i = 0; i < currentWorkers.size(); i++){
                mlcUser u = currentWorkers.get(i);
                if (u != CacheEngineStatic.getCurrentUser()){
                    wsb.append(u.getFirstName());
                    if (i < currentWorkers.size()-1) wsb.append(", ");
                }
            }
            if (!wsb.toString().isEmpty()){
                sb.append(", ").append(bundle.getString("AttributeStringBuilder_CurrentWorker")).append(wsb.toString());
            }
        }

        return sb.toString();
    }
}
