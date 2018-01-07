package com.mindliner.img.icons;

import com.mindliner.analysis.MlClassHandler.MindlinerObjectType;
import java.awt.Image;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * This class mainly translates object types to correct icon names.
 *
 * @author Marius Messerli
 */
public class MlIconManager {

    static {
        initializeTypeNames();
    }

    private static Map<MindlinerObjectType, String> imageNameMap;

    public static enum IconSize {

        sixteen,
        twentyFour,
        thirtyTwo,
        sixtyfour,
        onetwentyeight
    }

    private static void initializeTypeNames() {
        imageNameMap = new EnumMap<>(MindlinerObjectType.class);
        imageNameMap.put(MindlinerObjectType.Task, "checkbox.png");
        imageNameMap.put(MindlinerObjectType.Collection, "cubes_grey.png");
        imageNameMap.put(MindlinerObjectType.Any, "text.png");
        imageNameMap.put(MindlinerObjectType.News, "flash_red.png");
        imageNameMap.put(MindlinerObjectType.Knowlet, "information2.png");
        imageNameMap.put(MindlinerObjectType.Contact, "users2.png");
        imageNameMap.put(MindlinerObjectType.Image, "photo_landscape2.png");
        imageNameMap.put(MindlinerObjectType.Container, "layout.png");
        imageNameMap.put(MindlinerObjectType.Map, "chart_dot.png");
    }

    public static Image getImageForType(MindlinerObjectType type) {
        return getImageForType(type, IconSize.twentyFour);
    }

    public static Image getImageForType(MindlinerObjectType type, IconSize size) {
        return MlIconLoader.getImage(size, imageNameMap.get(type));

    }

    public static ImageIcon getIconForType(MindlinerObjectType type) {
        Image img = getImageForType(type, IconSize.thirtyTwo);
        if (img != null) {
            return new ImageIcon(img);
        }
        return null;
    }

    /**
     * Returns an icon for the type, in the case of a task returns a different
     * icon for completed and open tasks.
     *
     * @param type The object type
     * @param completed If the type is Task then this flag tells the subsystem
     * if the task is completed or not
     * @return
     */
    public static ImageIcon getIconForType(MindlinerObjectType type, boolean completed) {
        if (type.equals(MindlinerObjectType.Task)) {
            if (completed) {
                return MlIconLoader.getImageIcon(IconSize.thirtyTwo, "checkbox.png");
            } else {
                return MlIconLoader.getImageIcon(IconSize.thirtyTwo, "checkbox_unchecked.png");
            }
        }
        else return getIconForType(type);
    }
    
    public static ImageIcon getNodeExpandIcon(){
        return MlIconLoader.getImageIcon(IconSize.sixteen, "navigate_plus_dis.png");
    }
    
    public static ImageIcon getNodeCollapseIcon(){
        return MlIconLoader.getImageIcon(IconSize.sixteen, "navigate_minus_dis.png");
    }

    public static Image getErrorImage() {
        return MlIconLoader.getImage(IconSize.thirtyTwo, "document_delete.png");
    }
}
