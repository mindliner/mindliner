/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.LinkCommand;
import com.mindliner.commands.ModificationUpdateCommand;
import com.mindliner.commands.UnlinkCommand;
import com.mindliner.enums.LinkRelativeType;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;
import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.gui.MlDialogUtils;
import com.mindliner.main.MindlinerMain;

/**
 * This dialog loads the icons from the cache and lets the user toggle them for
 * the specified node.
 *
 * @author Marius Messerli
 */
public class IconGridDialog extends JDialog {

    private static final int ICON_SIZE = 32;

    MindlinerMapper mapper;
    MlMapNode node;
    List<MlcImage> icons;

    public IconGridDialog(MindlinerMapper mapper, MlMapNode node) {
        this.mapper = mapper;
        this.node = node;
        initialize();
    }

    private void initialize() {
        icons = CacheEngineStatic.getIcons(node.getObject().getClient());
        if (icons.isEmpty()) {
            return;
        }
        int gridRows = (int) (Math.sqrt(icons.size()) * 1.3);
        int gridCols = icons.size() / gridRows;
        GridLayout gridLayout = new GridLayout(gridCols, gridRows);
        gridLayout.setHgap(3);
        gridLayout.setVgap(3);
        setLayout(gridLayout);
        setTitle("Toggle icons for " + node + ". ESC to close");
        List<MlcImage> oIcns = node.getObject().getIcons();
        icons.stream().forEach((icon) -> {
            if (icon != null) {
                IconButton ib = new IconButton(icon, node, (Graphics2D) mapper.getGraphics());
                if (oIcns != null && oIcns.contains(icon)) {
                    ib.setSelected(true);
                } else {
                    ib.setSelected(false);
                }
                add(ib);
            }
        });
        setSize(new Dimension(500, 400));
        pack();
        setLocationRelativeTo(MindlinerMain.getInstance());
        setVisible(true);
        MlDialogUtils.addEscapeListener(this);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public boolean hasIcons() {
        return !icons.isEmpty();
    }

    private class IconButton extends JToggleButton {

        MlcImage icon;
        MlMapNode node;
        Graphics2D g2;

        public IconButton(final MlcImage icon, final MlMapNode node, Graphics2D g2) {
            this.icon = icon;
            this.node = node;
            this.g2 = g2;
            this.setToolTipText(icon.getDescription());
            final CommandRecorder cr = CommandRecorder.getInstance();
            Image scaledInstance = icon.getIcon().getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaledInstance));
            addActionListener((ActionEvent e) -> {
                mlcObject obj = node.getObject();
                if (obj.getIcons() == null) {
                    obj.setIcons(new ArrayList<>());
                }
                if (obj.getIcons().contains(icon)) {
                    obj.getIcons().remove(icon);
                    cr.scheduleCommand(new UnlinkCommand(obj, icon, true, LinkRelativeType.ICON_OBJECT));
                } else {

                    obj.getIcons().add(icon);
                    cr.scheduleCommand(new LinkCommand(obj, icon, true, LinkRelativeType.ICON_OBJECT));
                }
                /**
                 * Linking alone does not update the modification date. For
                 * objects decorated with icons, however, this is needed so that
                 * the new icons are shown server.
                 */
                cr.scheduleCommand(new ModificationUpdateCommand(obj, new Date()));
                mapper.repaint();
            });
        }

    }
}
