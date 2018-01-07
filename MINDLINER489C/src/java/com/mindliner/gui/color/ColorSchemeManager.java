/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.MlCacheException;
import com.mindliner.categories.*;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.entities.Colorizer.ColorizerRangeKeys;
import com.mindliner.entities.Colorizer.ColorizerThresholdKeys;
import com.mindliner.gui.color.FixedKeyColorizer.FixedKeys;
import com.mindliner.objects.transfer.MlTransferColorScheme;
import com.mindliner.objects.transfer.MltColor;
import com.mindliner.objects.transfer.MltColorizer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marius Messerli
 */
public class ColorSchemeManager {

    private static BaseColorizer getColorizerForClassName(String className) {
        for (BaseColorizer c : ColorManager.getColorizers()) {
            if (c.getClass().getName().equals(className)) {
                return c;
            }
        }
        return null;
    }

    public static void resetColors() {
        for (BaseColorizer c : ColorManager.getColorizers()) {
            c.initializeSensibleDefaults();
        }
    }

    /**
     * In this process, the client is the driver. The client has all the
     * colorizer it needs and when loading a scheme those part of the scheme
     * that are applicable are loaded. In the future it is conceivable that the
     * scheme has information on a colorizer that no longer exists - that
     * information will be ignored.
     *
     * Therefore we cycle through the client colorizers and not through the
     * server side colorizers during the import (and also export).
     *
     * @param scheme
     */
    public static void importColorScheme(MlTransferColorScheme scheme) {

        for (MltColorizer tColorizer : scheme.getColorizers()) {
            BaseColorizer cColorizer = getColorizerForClassName(tColorizer.getColorizerClassName());
            if (cColorizer != null) {

                if (cColorizer instanceof TaskPriorityColorizer) {
                    for (MltColor tColor : tColorizer.getColors()) {
                        mlsPriority v = CacheEngineStatic.getPriority(tColor.getDriverValue());
                        if (v != null) {
                            cColorizer.setColor(v, tColor.getColor());
                        }
                    }

                } else if (cColorizer instanceof ConfidentialityColorizer) {
                    // confis are data pool specific so remove those from non-shared pools
                    List<Integer> poolIds = CacheEngineStatic.getCurrentUser().getClientIds();
                    for (MltColor tColor : tColorizer.getColors()) {
                        mlsConfidentiality v = CacheEngineStatic.getConfidentiality(tColor.getDriverValue());
                        if (v != null && poolIds.contains(v.getClient().getId())) {
                            cColorizer.setColor(v, tColor.getColor());
                        }
                    }
                } else if (cColorizer instanceof ModificationAgeColorizer) {
                    ModificationAgeColorizer mac = (ModificationAgeColorizer) cColorizer;
                    mac.setThreshold(tColorizer.getMinimumOrThreshold());
                    for (MltColor tColor : tColorizer.getColors()) {
                        // need the following check as enum types may have been deleted in the code but still exist in the database
                        for (ColorizerThresholdKeys key : ColorizerThresholdKeys.values()) {
                            if (key.ordinal() == tColor.getDriverValue()) {
                                cColorizer.setColor(key, tColor.getColor());
                            }
                        }
                    }
                } else if (cColorizer instanceof RatingLinearColorizer) {
                    RatingLinearColorizer rlc = (RatingLinearColorizer) cColorizer;
                    rlc.setMinimum(tColorizer.getMinimumOrThreshold());
                    rlc.setMaximum(tColorizer.getMaximum());
                    for (MltColor tColor : tColorizer.getColors()) {
                        for (ColorizerRangeKeys key : ColorizerRangeKeys.values()) {
                            if (tColor.getDriverValue() == key.ordinal()) {
                                cColorizer.setColor(key, tColor.getColor());
                            }
                        }
                    }
                } else if (cColorizer instanceof OwnerColorizer) {
                    for (MltColor tColor : tColorizer.getColors()) {
                        mlcUser owner = null;
                        try {
                            // here we automatically filter users that do not have a shared client
                            owner = CacheEngineStatic.getUser(tColor.getDriverValue());
                            cColorizer.setColor(owner, tColor.getColor());
                        } catch (MlCacheException ex) {
                            // nothing spectacular, probably just an owner of an out-of-reach data pool
                        }
                    }
                } else if (cColorizer instanceof FixedKeyColorizer) {

                    // @todo this is not very robust, if someone changes the ordering in FixedKeys the colors come our the wrong way
                    // not too horrible either, though....
                    FixedKeys[] values = FixedKeyColorizer.FixedKeys.values();
                    for (MltColor tColor : tColorizer.getColors()) {
                        FixedKeys key = values[tColor.getDriverValue()];
                        if (key != null) {
                            cColorizer.setColor(key, tColor.getColor());
                        }
                    }
                } else if (cColorizer instanceof DataPoolColorizer) {
                    for (MltColor tColor : tColorizer.getColors()) {
                        mlcClient client = CacheEngineStatic.getClient(tColor.getDriverValue());
                        if (client != null) {
                            cColorizer.setColor(client, tColor.getColor());
                        }
                    }
                }
            }
        }
    }

    public static MlTransferColorScheme createTransferColorScheme(String name) {
        MlTransferColorScheme scheme = new MlTransferColorScheme();
        scheme.setName(name);
        List<MltColorizer> colorizers = new ArrayList<>();
        for (BaseColorizer cColorizer : ColorManager.getColorizers()) {
            MltColorizer tColorizer = new MltColorizer();
            tColorizer.setColorizerClassName(cColorizer.getClass().getName());
            tColorizer.setType(cColorizer.getType());
            switch (tColorizer.getType()) {
                case Threshold:
                    ThresholdColorizer tc = (ThresholdColorizer) cColorizer;
                    tColorizer.setMinimumOrThreshold(tc.getThreshold());
                    break;

                case Continuous:
                    RangeColorizer rc = (RangeColorizer) cColorizer;
                    tColorizer.setMinimumOrThreshold(rc.getMinimum());
                    tColorizer.setMaximum(rc.getMaximum());
                    break;
            }
            List<MltColor> colors = new ArrayList<>();
            for (Object key : cColorizer.getColorMap().keySet()) {
                Color cColor = (Color) cColorizer.getColorMap().get(key);
                if (cColor != null) {
                    MltColor tColor = new MltColor();
                    tColor.setColor(cColor);
                    tColor.setDriverValue(cColorizer.getKeyId(key));
                    colors.add(tColor);
                }
            }
            tColorizer.setColors(colors);
            colorizers.add(tColorizer);
        }
        scheme.setColorizers(colorizers);
        return scheme;
    }
}
