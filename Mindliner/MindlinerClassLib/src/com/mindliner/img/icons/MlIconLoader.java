/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.img.icons;

import com.mindliner.img.icons.MlIconManager.IconSize;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * This class loads icons for the entire client application so that resource
 * path changes become transparent to the rest of hte application. It also
 * caches the loaded for the duration of a session.
 *
 * @author Marius Messerli Created on 27.08.2012, 15:10:27
 */
public class MlIconLoader {

    private static final String WORKSPHEREMAP_ICON_PATH = "3232/wsm/";
    private static final List<String> workSphereMapIconNames = new ArrayList<>();
    private static final Map<String, Image> imageCache = new HashMap<>();
    private static final Map<String, Image> workSphereMapCache = new HashMap<>();

    static {
        workSphereMapIconNames.add("airmail_closed.png");
        workSphereMapIconNames.add("arrow2_down_blue.png");
        workSphereMapIconNames.add("arrow2_up_blue.png");
        workSphereMapIconNames.add("calculator.png");
        workSphereMapIconNames.add("calendar_5.png");
        workSphereMapIconNames.add("chart_line.png");
        workSphereMapIconNames.add("checkbox.png");
        workSphereMapIconNames.add("clock.png");
        workSphereMapIconNames.add("data_blue.png");
        workSphereMapIconNames.add("delete.png");
        workSphereMapIconNames.add("document_edit.png");
        workSphereMapIconNames.add("help2.png");
        workSphereMapIconNames.add("paperclip.png");
        workSphereMapIconNames.add("presentation.png");
        workSphereMapIconNames.add("printer.png");
        workSphereMapIconNames.add("sign_warning.png");
        workSphereMapIconNames.add("symbol_dollar.png");
        workSphereMapIconNames.add("symbol_euro.png");
        workSphereMapIconNames.add("table2_selection_row.png");
        workSphereMapIconNames.add("user.png");
        workSphereMapIconNames.add("user2.png");
        workSphereMapIconNames.add("user3.png");
        workSphereMapIconNames.add("users2.png");
    }

    public static List<String> getWorkSphereMapIconNames() {
        return workSphereMapIconNames;
    }

    /**
     * Returns subdirectory with trailing slashes
     */
    private static String getSubDirectory(IconSize size) {
        switch (size) {
            case sixteen:
                return "1616/";

            case twentyFour:
                return "2424/";

            case thirtyTwo:
                return "3232/";

            case sixtyfour:
                return "6464/";

            case onetwentyeight:
                return "128128/";

            default:
                throw new IllegalArgumentException("unknown icon size requested");
        }
    }

    public static ImageIcon getImageIcon(IconSize size, String name) {
        return new ImageIcon(getImage(size, name));
    }

    public static Image getImage(IconSize size, String name) {
        String key = size.name().concat(name);
        Image img = imageCache.get(key);
        if (img != null) {
            return img;
        }
        try {
            BufferedImage bimg = ImageIO.read(MlIconLoader.class.getResource(getSubDirectory(size) + name));
            imageCache.put(key, bimg);
            return bimg;
        } catch (IOException ex) {
            Logger.getLogger(MlIconLoader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static Image getWorkSphereMapIcon(String name) {
        Image img = workSphereMapCache.get(name);
        if (img != null) {
            return img;
        }
        try {
            String imgName = WORKSPHEREMAP_ICON_PATH + name;
            BufferedImage bufferedImage = ImageIO.read(MlIconLoader.class.getResource(imgName));
            workSphereMapCache.put(name, bufferedImage);
            return bufferedImage;
        } catch (IOException ex) {
            Logger.getLogger(MlIconLoader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static ImageIcon getAboutScreenIcon(){
        return new ImageIcon(MlIconLoader.class.getResource("About.png"));
    }

    public static ImageIcon getApplicationIcon() {
        return new ImageIcon(MlIconLoader.class.getResource("MindlinerApplicationIcon.png"));
    }
}
