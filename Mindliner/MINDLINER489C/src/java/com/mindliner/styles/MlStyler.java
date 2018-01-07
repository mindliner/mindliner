/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.styles;

import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.gui.tablemodels.MlTableHeaderCellRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIDefaults;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * This class is used to style Swing components to be in synch with the WebApp
 *
 * @author Marius Messerli
 */
public class MlStyler {

    public static final Color BACKGROUND_CYAN = new Color(0xd7e3ed);
    public static final Color LIGHT_BLUE_GREEN_BACKGROUND = new Color(246, 253, 254);
    public static final Color DARK_GREEN_TITLE_COLOR = new Color(0x337373);
    private static boolean brightBackground = true;
    private static final String BRIGHT_BACKGROUND_KEY = "brightbackground";
    private static final Preferences preferences;

    static {
        preferences = Preferences.userNodeForPackage(MlStyler.class);
        brightBackground = preferences.getBoolean(BRIGHT_BACKGROUND_KEY, brightBackground);
        System.out.println("MLStyler light background is " + brightBackground);
    }

    public static boolean isLightBackground() {
        return brightBackground;
    }

    public static void setLightBackground(boolean lightBackground) {
        MlStyler.brightBackground = lightBackground;
    }

    public static void colorizeButton(JButton button, BaseColorizer colorizer) {
        Color bg = colorizer.getColorForKey(FixedKeyColorizer.FixedKeys.BUTTON_BACKGROUND);
        button.setBorderPainted(true);
        button.setFocusPainted(true);
        button.setContentAreaFilled(isLightBackground());
        button.setBackground(bg);
        if (!brightBackground) {
            BevelBorder bb = new BevelBorder(BevelBorder.RAISED, bg.brighter(), bg.darker());
            button.setBorder(bb);
        }
    }

    public static void colorSplitPane(JSplitPane sp, Color bg, Color borderColor) {
        sp.setBorder(new EmptyBorder(0, 0, 0, 0));
        sp.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {

                    @Override
                    public void paint(Graphics g) {
                        g.setColor(bg);
                        g.fillRect(0, 0, getSize().width, getSize().height);
                        super.paint(g);
                    }

                    @Override
                    public Border getBorder() {
                        return BorderFactory.createMatteBorder(0, 0, 0, 1, borderColor);
                    }
                };
            }

        });
    }

    public static void colorTabbedPane(JTabbedPane tp, Color fg, Color bg) {
        tp.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                g.setColor(bg);
                super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setColor(fg);
                super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                g.setColor(bg);
                super.paintTabArea(g, tabPlacement, selectedIndex); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            }

        });
    }

    public static void colorizeComboBox(JComboBox cb, FixedKeyColorizer fkc) {
        cb.setForeground(fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT));
        cb.setBackground(fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_BACKGROUND));
        if (!brightBackground) {
            cb.setRenderer(new MlComboboxCellRenderer(fkc));
            cb.setUI(new MlComboBoxUI(fkc));
        }
    }

    public static void colorizeTableHeader(JTable table, Color fg, Color bg) {
        table.getTableHeader().setForeground(fg);
        table.getTableHeader().setDefaultRenderer(new MlTableHeaderCellRenderer(fg, bg));
    }

    public static void colorizeTextPane(JTextPane p, Color bg, Color fg) {
        UIDefaults defaults = new UIDefaults();
        defaults.put("EditorPane[Enabled].backgroundPainter", bg);
        p.putClientProperty("Nimbus.Overrides", defaults);
        p.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
        p.setBackground(bg);
    }

    public static void colorizeCheckbox(JCheckBox box, Color fg, Color bg) {
        box.setForeground(fg);
        box.setBackground(bg);
    }

    public static void storePreferences() {
        preferences.putBoolean(BRIGHT_BACKGROUND_KEY, brightBackground);
    }

}
