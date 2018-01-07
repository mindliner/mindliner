/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.common;

import javax.swing.JCheckBox;

/**
 *
 * @author dominic
 */
public class ObjectCheckBox extends JCheckBox {

  private Object value = null;

  public ObjectCheckBox(Object itemValue, boolean selected) {
    super(itemValue == null ? "" : "" + itemValue, selected);
    this.value = itemValue;
  }

  @Override
  public boolean isSelected() {
    return super.isSelected();
  }

  @Override
  public void setSelected(boolean selected) {
    super.setSelected(selected);
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }
}
