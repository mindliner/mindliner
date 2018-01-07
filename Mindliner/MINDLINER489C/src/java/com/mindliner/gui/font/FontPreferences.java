/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.font;

import com.mindliner.system.MlSessionClientParams;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import javax.swing.UIManager;

/**
 * This is a convenience class to store font preferences.
 *
 * @author Marius Messerli
 */
public class FontPreferences {

    private static final String FONT_BASE_KEY = "mlfont";
    private static final String FONT_COUNT_KEY = "mlfontcount";
    private static final Map<String, Font> fontPrefMap = new HashMap<>();

    private static final String FONT_NAME_KEY = "name";
    private static final String FONT_SIZE_KEY = "size";
    private static final String FONT_STYLE_KEY = "style";

    private static Font getStandardFont() {
        // we use the label object to objectain the standard size on the system
        JLabel l = new JLabel();
        return new Font(MlSessionClientParams.DEFAULT_FONT_FAMILY, Font.PLAIN, l.getFont().getSize() + 2);
    }

    public static void storePreferences() {
        Preferences p = Preferences.userNodeForPackage(FontPreferences.class);

        // first store all keys so we can load them again
        p.putInt(FONT_COUNT_KEY, fontPrefMap.size());
        List<String> keyList = new ArrayList<>(fontPrefMap.keySet());
        for (int i = 0; i < keyList.size(); i++) {
            String key = keyList.get(i);
            p.put(FONT_BASE_KEY + i, key);
        }

        // then store all key values
        for (String key : fontPrefMap.keySet()) {
            String fullKey = FONT_BASE_KEY.concat(key).concat(FONT_NAME_KEY);
            p.put(fullKey, fontPrefMap.get(key).getName());

            fullKey = FONT_BASE_KEY.concat(key).concat(FONT_STYLE_KEY);
            p.putInt(fullKey, fontPrefMap.get(key).getStyle());

            fullKey = FONT_BASE_KEY.concat(key).concat(FONT_SIZE_KEY);
            p.putInt(fullKey, fontPrefMap.get(key).getSize());
        }
    }

    public static void loadPreferences() {
        fontPrefMap.clear();
        Preferences p = Preferences.userNodeForPackage(FontPreferences.class);
        int keyCount = p.getInt(FONT_COUNT_KEY, 0);
        Font defaultFont = UIManager.getDefaults().getFont("Label.font");
        for (int i = 0; i < keyCount; i++) {
            String key = p.get(FONT_BASE_KEY + i, "");
            if (!key.isEmpty()) {
                String fullKey = FONT_BASE_KEY.concat(key).concat(FONT_NAME_KEY);
                String fontName = p.get(fullKey, defaultFont.getFamily());

                fullKey = FONT_BASE_KEY.concat(key).concat(FONT_STYLE_KEY);
                int fontStyle = p.getInt(fullKey, defaultFont.getStyle());

                fullKey = FONT_BASE_KEY.concat(key).concat(FONT_SIZE_KEY);
                int fontSize = p.getInt(fullKey, defaultFont.getSize());

                Font f = new Font(fontName, fontStyle, fontSize);

                fontPrefMap.put(key, f);
            }
        }
    }

    /**
     * Stores the specified value under the specified key.
     *
     * @param identifyer A key describing a use case for the File Dialog
     * (PPTImport or PictureExport, or something)
     * @param font The font to be associated wit the key
     */
    public static void setFont(String identifyer, Font font) {
        fontPrefMap.put(identifyer, font);
    }

    /**
     * Returns a smart default for a file dialog.
     *
     * @param identifyer The key associated with the font
     *
     * @return The folder location associated with the key
     */
    public static Font getFont(String identifyer) {
        Font f = fontPrefMap.get(identifyer);
        if (f == null) {
            f = getStandardFont();
        }
        return f;
    }
}
