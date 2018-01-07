package com.mindliner.gui.color;

import com.mindliner.clientobjects.MlcLink;
import com.mindliner.gui.color.FixedKeyColorizer.FixedKeys;
import com.mindliner.styles.MlStyler;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;

/**
 *
 * This class provides the default colors for the Mindliner application. Each
 * color key listed below can futher be customized by the user in the
 * preferences and color schemes can be shared between users.
 *
 * @author Marius Messerli Created on 30.09.2012, 14:43:07
 */
public class FixedKeyColorizer extends BaseColorizer<FixedKeys> {

    public static enum FixedKeys {

        MAIN_DEFAULT_BACKGROUND,
        MAIN_DEFAULT_TEXT,
        TABLE_GRID,
        TABLE_BACKGROUND,
        TABLE_CELL_BACKGROUND,
        TABLE_HEADER_BACKGROUND,
        MAIN_COMBOBOX_SELECTION,
        TABLE_HEADER_TEXT,
        SPREADSHEET_SELECTION_BACKGROUND,
        SPREADSHEET_SELECTION_FOREGROUND,
        SPREADSHEET_CELL_BACKGROUND,
        SPREADSHEET_POSITIVE_CELL_FOREGROUND,
        SPREADSHEET_NEGATIVE_CELL_FOREGROUND,
        SPREADSHEET_HEADER_BACKGROUND,
        SPREADSHEET_HEADER_FOREGROUND,
        SPREADSHEET_NOTES_BACKGROUND,
        MAP_BACKGROUND,
        MAP_NODE_BACKGROUND,
        MAP_DESCRIPTION_SEPARATOR,
        MAP_NODE_JUST_MODIFIED,
        MAP_LEVEL_ZERO,
        MAP_LEVEL_ONE,
        MAP_LEVEL_TWO,
        MAP_LEVEL_THREE,
        MAP_LEVEL_FOUR,
        WEEKPLAN_EVEN_ROWS,
        WEEKPLAN_ODD_ROWS,
        WEEKPLAN_SELECTION_BACKGROUND,
        WEEKPLAN_SELECTION_FONT,
        WEEKPLAN_CURRENT_DAY,
        WEEKPLAN_TOGO,
        WEEKPLAN_CURRENT_TASK_BACKGROUND,
        WEEKPLAN_FONT,
        WEEKPLAN_BACKGROUND,
        WEEKPLAN_CALENDER_PANEL,
        CONTAINER_APPLICATION,
        CONTAINER_ORGANIZATION,
        CONTAINER_PROCESS,
        CONTAINER_DESCRIPTION,
        CONTAINER_BACKGROUND,
        CONTAINER_EXTRA_RELATIVES_INDICATOR, // an indicator that shows if a WSM node has relatives not shown on the WSM
        LOGIN_DIALOG_TEXT_COLOR,
        LOGIN_DIALOG_BACKGROUND_COLOR,
        BUTTON_BACKGROUND,
        MAIN_COMBOBOX_FOCUS,
        MAIN_DEFAULT_TEXT_CARET,
        WEEKPLAN_CURRENT_TEAM_TASK_BACKGROUND // a task on which a team member is working on
    }

    public FixedKeyColorizer(Color defaultColor) {
        super(defaultColor);
    }

    @Override
    public List<FixedKeys> getInputValueList() {
        return Arrays.asList(FixedKeys.values());
    }

    @Override
    public Color getColorForObject(Object keyObject) {
        FixedKeys key = (FixedKeys) keyObject;
        Color c = colorMap.get(key);
        if (c == null) {
            return getDefaultColor();
        }
        return c;
    }

    @Override
    public Color getColorForLink(MlcLink link) {
        return getDefaultColor();
    }

    @Override
    public int getKeyId(FixedKeys key) {
        return key.ordinal();
    }

    @Override
    public void initializeSensibleDefaults() {
        colorMap.clear();
        colorMap.put(FixedKeys.MAIN_DEFAULT_BACKGROUND, MlStyler.LIGHT_BLUE_GREEN_BACKGROUND);
        colorMap.put(FixedKeys.MAIN_DEFAULT_TEXT, new Color(1, 115, 148));
        colorMap.put(FixedKeys.BUTTON_BACKGROUND, MlStyler.LIGHT_BLUE_GREEN_BACKGROUND.brighter());
        colorMap.put(FixedKeys.TABLE_BACKGROUND, MlStyler.LIGHT_BLUE_GREEN_BACKGROUND);
        colorMap.put(FixedKeys.TABLE_CELL_BACKGROUND, MlStyler.LIGHT_BLUE_GREEN_BACKGROUND);
        colorMap.put(FixedKeys.TABLE_GRID, new Color(212, 232, 235));
        colorMap.put(FixedKeys.TABLE_HEADER_BACKGROUND, new Color(192, 220, 224));
        colorMap.put(FixedKeys.MAIN_COMBOBOX_SELECTION, Color.green);
        colorMap.put(FixedKeys.MAIN_DEFAULT_TEXT_CARET, Color.green);

        colorMap.put(FixedKeys.MAIN_COMBOBOX_FOCUS, new Color(255, 254, 241));
        colorMap.put(FixedKeys.TABLE_HEADER_TEXT, new Color(1, 115, 148));
        colorMap.put(FixedKeys.SPREADSHEET_SELECTION_BACKGROUND, new Color(192, 219, 239));
        colorMap.put(FixedKeys.SPREADSHEET_SELECTION_FOREGROUND, Color.BLACK);
        colorMap.put(FixedKeys.SPREADSHEET_CELL_BACKGROUND, Color.WHITE);
        colorMap.put(FixedKeys.SPREADSHEET_POSITIVE_CELL_FOREGROUND, Color.BLACK);
        colorMap.put(FixedKeys.SPREADSHEET_NEGATIVE_CELL_FOREGROUND, new Color(183, 0, 0));
        colorMap.put(FixedKeys.SPREADSHEET_HEADER_BACKGROUND, new Color(241, 241, 241));
        colorMap.put(FixedKeys.SPREADSHEET_HEADER_FOREGROUND, new Color(8, 8, 8));
        colorMap.put(FixedKeys.SPREADSHEET_NOTES_BACKGROUND, new Color(205, 239, 207));
        colorMap.put(FixedKeys.MAP_NODE_BACKGROUND, Color.WHITE);
        colorMap.put(FixedKeys.MAP_NODE_JUST_MODIFIED, new Color(182, 234, 190));
        colorMap.put(FixedKeys.MAP_DESCRIPTION_SEPARATOR, new Color(245, 175, 206));
        colorMap.put(FixedKeys.MAP_BACKGROUND, MlStyler.LIGHT_BLUE_GREEN_BACKGROUND);
        colorMap.put(FixedKeys.MAP_LEVEL_ZERO, Color.black);
        colorMap.put(FixedKeys.MAP_LEVEL_ONE, new Color(40, 20, 128));
        colorMap.put(FixedKeys.MAP_LEVEL_TWO, new Color(120, 20, 50));
        colorMap.put(FixedKeys.MAP_LEVEL_THREE, new Color(20, 120, 40));
        colorMap.put(FixedKeys.MAP_LEVEL_FOUR, new Color(128, 128, 60));
        colorMap.put(FixedKeys.WEEKPLAN_CURRENT_DAY, new Color(179, 4, 45));
        colorMap.put(FixedKeys.WEEKPLAN_EVEN_ROWS, MlStyler.LIGHT_BLUE_GREEN_BACKGROUND);
        colorMap.put(FixedKeys.WEEKPLAN_ODD_ROWS, new Color(227, 247, 249));
        colorMap.put(FixedKeys.WEEKPLAN_FONT, Color.BLACK);
        colorMap.put(FixedKeys.WEEKPLAN_TOGO, new Color(2, 2, 189));
        colorMap.put(FixedKeys.WEEKPLAN_SELECTION_BACKGROUND, new Color(236, 254, 214));
        colorMap.put(FixedKeys.WEEKPLAN_SELECTION_FONT, Color.BLACK);
        colorMap.put(FixedKeys.WEEKPLAN_BACKGROUND, MlStyler.LIGHT_BLUE_GREEN_BACKGROUND);
        colorMap.put(FixedKeys.WEEKPLAN_CURRENT_TASK_BACKGROUND, new Color(209, 211, 254));
        colorMap.put(FixedKeys.WEEKPLAN_CURRENT_TEAM_TASK_BACKGROUND, new Color(254, 233, 205));
        colorMap.put(FixedKeys.WEEKPLAN_CALENDER_PANEL, MlStyler.LIGHT_BLUE_GREEN_BACKGROUND);
        colorMap.put(FixedKeys.CONTAINER_APPLICATION, new Color(156, 0, 0));
        colorMap.put(FixedKeys.CONTAINER_ORGANIZATION, new Color(126, 11, 104));
        colorMap.put(FixedKeys.CONTAINER_PROCESS, new Color(0, 128, 64));
        colorMap.put(FixedKeys.CONTAINER_DESCRIPTION, Color.GRAY);
        colorMap.put(FixedKeys.CONTAINER_DESCRIPTION, MlStyler.LIGHT_BLUE_GREEN_BACKGROUND);
        colorMap.put(FixedKeys.CONTAINER_EXTRA_RELATIVES_INDICATOR, MlStyler.LIGHT_BLUE_GREEN_BACKGROUND);
    }

}
