/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import com.mindliner.image.LazyImage;
import com.mindliner.image.ScaledImageLabel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;

/**
 *
 * @author Dominic Plangger, Marius Messerli
 */
public class ImageFullscreenViewer {

    public static void showFullScreen(LazyImage img) {
        final JDialog fsv = new JDialog();
        fsv.setLayout(new BorderLayout());
        fsv.setUndecorated(true);
        fsv.setModal(true);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        ScaledImageLabel big = new ScaledImageLabel(
                Math.min(screen.width, img.getImage().getWidth(null)),
                Math.min(screen.height, img.getImage().getHeight(null)), ScaledImageLabel.InitialZoomPrefs.Original);
        big.setImage(img);
        fsv.add(big, BorderLayout.CENTER);
        fsv.setPreferredSize(new Dimension(big.getWidth(), big.getHeight()));
        fsv.setLocation(DialogPositioner.getLocation(big.getWidth(), big.getHeight()));
        big.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                fsv.setVisible(false);
                fsv.dispose();
            }
        });
        fsv.pack();
        fsv.setVisible(true);
    }

}
