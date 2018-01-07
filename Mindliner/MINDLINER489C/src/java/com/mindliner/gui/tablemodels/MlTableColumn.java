/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.tablemodels;

/**
 *
 * @author Marius Messerli
 */
public class MlTableColumn {

    private Class contentClass = null;
    private String header = "";
    private int width = 30;

    public MlTableColumn(String header, Class contentClass) {
        this.header = header;
        this.contentClass = contentClass;
    }

    public String getHeader() {
        return header;
    }

    public Class getContentClass() {
        return contentClass;
    }

    public void setWidth(int i) {
        width = i;
    }

    public int getWidth() {
        return width;
    }
}
