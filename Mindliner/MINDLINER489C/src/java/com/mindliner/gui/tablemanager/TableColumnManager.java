/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.tablemanager;

import com.mindliner.analysis.MlClassHandler.MindlinerObjectType;
import com.mindliner.categories.*;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.entities.Colorizer;
import com.mindliner.gui.tablemodels.MlTableColumn;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class contains the logic for the columns for each table.
 *
 * @author Marius Messerli
 */
public class TableColumnManager {

    private static void setupHeadline(List<MlTableColumn> cols) {
        MlTableColumn tc = new MlTableColumn("Headline", mlcUser.class);
        cols.add(tc);
    }

    private static void setupCommonTrailingColumns(List<MlTableColumn> cols) {
        MlTableColumn tc = new MlTableColumn("Own", String.class);
        cols.add(tc);
        tc = new MlTableColumn("Mod", Date.class);
        cols.add(tc);
        tc = new MlTableColumn("Cfd", mlsConfidentiality.class);
        cols.add(tc);
    }

    public static List<MlTableColumn> getAvailableColumns(MindlinerObjectType type) {
        List<MlTableColumn> cols = new ArrayList<>();

        switch (type) {

            case Collection:
                setupHeadline(cols);
                setupCommonTrailingColumns(cols);
                break;
            case Knowlet:
                setupHeadline(cols);
                setupCommonTrailingColumns(cols);
                break;
            case Contact:
                MlTableColumn tc = new MlTableColumn("First", String.class);
                cols.add(tc);
                tc = new MlTableColumn("Middle", String.class);
                cols.add(tc);
                tc = new MlTableColumn("Last", String.class);
                cols.add(tc);
                tc = new MlTableColumn("Email", String.class);
                cols.add(tc);
                setupCommonTrailingColumns(cols);
                break;

            case Any:

                setupHeadline(cols);
                tc = new MlTableColumn("Icn", Image.class);
                cols.add(tc);
                setupCommonTrailingColumns(cols);
                break;

            case Task:
                setupHeadline(cols);

                tc = new MlTableColumn("x", Boolean.class);
                cols.add(tc);

                tc = new MlTableColumn("Prio", mlsPriority.class);
                cols.add(tc);

                tc = new MlTableColumn("Due", Date.class);
                cols.add(tc);

                tc = new MlTableColumn("Effrt (m)", Integer.class);
                cols.add(tc);

                setupCommonTrailingColumns(cols);
                break;

            case Image:

                setupHeadline(cols);

                tc = new MlTableColumn("Img", Image.class);
                cols.add(tc);

                tc = new MlTableColumn("URL", String.class);
                cols.add(tc);

                setupCommonTrailingColumns(cols);

                break;

            default:
                setupHeadline(cols);
                setupCommonTrailingColumns(cols);
                break;
        }
        return cols;
    }

    /**
     * Returns color driver attributes that are meaningful to the specified
     * table.
     *
     * @param table The table for which the meaningful color drivers are
     * requested
     * @return
     */
    public static Set<Colorizer.ColorDriverAttribute> getSupportedAttributes(MlObjectTable table) {
        Set<Colorizer.ColorDriverAttribute> drivers = new HashSet<>();

        // first add the specific drivers
        if (table.getType() == MindlinerObjectType.Task) {
            drivers.add(Colorizer.ColorDriverAttribute.TaskPriority);
        }
        // now append the drivers that are common for all tables
        drivers.add(Colorizer.ColorDriverAttribute.Confidentiality);
        drivers.add(Colorizer.ColorDriverAttribute.Owner);
        drivers.add(Colorizer.ColorDriverAttribute.Rating);
        drivers.add(Colorizer.ColorDriverAttribute.ModificationAge);
        drivers.add(Colorizer.ColorDriverAttribute.DataPool);
        return drivers;
    }
}
