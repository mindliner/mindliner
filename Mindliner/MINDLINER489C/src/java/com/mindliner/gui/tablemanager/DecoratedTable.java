/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.gui.tablemanager;

import com.mindliner.exporter.MindlinerTransferHandler.LinkMode;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import javax.swing.JTable;

/**
 *
 * @author messerli
 */
public class DecoratedTable extends JTable{

    public DecoratedTable(MlObjectTable st) {
        mainTable = st;
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        DropLocation loc= getDropLocation();
        if (loc == null) {
            return;
        }
        renderPrettyIndicatorAt(loc, g);
    }

    private void renderPrettyIndicatorAt(DropLocation l, Graphics g){
        Font f = new Font("Default", Font.BOLD, 12); 
        g.setFont(f);
        drawSourceDependentIndicator(l, g);
    }

    private void drawSourceDependentIndicator(javax.swing.JTable.DropLocation l, Graphics g){
        int dropx = l.getDropPoint().x;
        int dropy = l.getDropPoint().y;

        if (getLinkMode(l, this) == LinkMode.Link) {
            g.setColor(Color.BLACK);
            g.drawString("+", dropx, dropy);
        }
        else {
            g.setColor(Color.RED);
            g.drawString("-", dropx, dropy);
        }
    }

    public static LinkMode getLinkMode(DropLocation dl, DecoratedTable table){
        Point dropPoint = dl.getDropPoint();
        // if dropped left then add, else remove
        if (table.getWidth() / 2 > dropPoint.x) return LinkMode.Link;
        else return LinkMode.Unlink;
    }

    public MlObjectTable getMainTable() {
        return mainTable;
    }

    private MlObjectTable mainTable = null;

}
