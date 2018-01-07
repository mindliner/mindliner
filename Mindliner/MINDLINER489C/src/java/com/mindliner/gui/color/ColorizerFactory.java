/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.entities.Colorizer.ColorDriverAttribute;
import java.awt.Color;

/**
 * This class produces Colorizer classes.
 *
 * @author Marius Messerli
 * @since 16-apr-2012
 */
public class ColorizerFactory {

    public static BaseColorizer createColorizer(ColorDriverAttribute attrib) {
        BaseColorizer c;
        switch (attrib) {

            case Confidentiality:
                c = new ConfidentialityColorizer(Color.darkGray);
                break;

            case TaskPriority:
                c = new TaskPriorityColorizer(Color.darkGray);
                break;

            case Owner:
                c = new OwnerColorizer(Color.darkGray);
                break;

            case DataPool:
                c = new DataPoolColorizer(new Color(20, 20, 80));
                break;

            case ModificationAge:
                c = new ModificationAgeColorizer(Color.darkGray);
                break;

            case Rating:
                c = new RatingLinearColorizer(Color.darkGray);
                break;

            case FixedKey:
                c = new FixedKeyColorizer(new Color(228, 228, 228));
                break;
                
            case Brizwalk:
                c = new BrizwalkStaticColorizer(Color.lightGray);
                break;

            default:
                throw new AssertionError();
        }
        c.setDriverAttribute(attrib);
        return c;
    }
}
