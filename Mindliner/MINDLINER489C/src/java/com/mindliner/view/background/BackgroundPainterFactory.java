/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.background;

import com.mindliner.view.background.DefaultBackgroundPainter.BackgroundPainterType;
import com.mindliner.view.background.ImageBackgroundPainter.ImageSample;
import com.mindliner.view.background.ImageBackgroundPainter.Layout;
import java.awt.Color;
import java.net.URL;

/**
 * This class generates background painters for the mind map view.
 *
 * @author Marius Messerli
 */
public class BackgroundPainterFactory {

    /**
     * Creates a new background painter.
     * 
     * @param type The painter type
     * @param image The image. For types other than Image this parameter is ignored
     * @param imageLayout The image layout. For types other than Image this parameter is ignored
     * @return 
     */
    public static BackgroundPainter createBackgroundPainter(BackgroundPainterType type, ImageSample image, Layout imageLayout) {
        switch (type) {

            case SingleColor:
                DefaultBackgroundPainter dbp = new DefaultBackgroundPainter();
                dbp.initialize();
                return dbp;

            case Image:
                URL url;
                switch (image) {
                    
                    case Bamboo:
                        url = BackgroundPainterFactory.class.getResource("/com/mindliner/img/background/BambooLight.jpg");
                        break;
                        
                    case CobbleStone:
                        url = BackgroundPainterFactory.class.getResource("/com/mindliner/img/background/Cobblestone.jpg");
                        break;
                        
                    case Dunes:
                        url = BackgroundPainterFactory.class.getResource("/com/mindliner/img/background/Dunes.jpg");
                        break;
                        
                    case Jeans:
                        url = BackgroundPainterFactory.class.getResource("/com/mindliner/img/background/JeansBackgroundBrighter.jpg");
                        break;
                        
                    case KahnDhaka:
                        url = BackgroundPainterFactory.class.getResource("/com/mindliner/img/background/Kahn-Dhaka-Capitol-20130515-005630-G-Fade.jpg");
                        break;
                        
                    case KahnFDRPark:
                        url = BackgroundPainterFactory.class.getResource("/com/mindliner/img/background/FDR-Kahn-sketch-we_631-B-Fade.jpg");
                        break;
                        
                    case BlackBoard:
                        url = BackgroundPainterFactory.class.getResource("/com/mindliner/img/background/BlackBoard.jpg");
                        break;
                        
                    case ParkInRome:
                        url = BackgroundPainterFactory.class.getResource("/com/mindliner/img/background/park_rome_lb.jpg");
                        break;
                        
                    default:
                        throw new AssertionError();
                }
                ImageBackgroundPainter ibp = new ImageBackgroundPainter(url);
                ibp.setLayout(imageLayout);
                return ibp;

            default:
                throw new AssertionError();
        }
    }
}
