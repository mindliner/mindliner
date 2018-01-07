/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.MlsImage;
import com.mindliner.entities.MlsImage.ImageType;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.main.MindlinerMain;
import com.mindliner.managers.ImageManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.awt.Image;
import javax.naming.NamingException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class ImageUpdateCommand extends MindlinerOnlineCommand {

    private ImageIcon previousImage = null;
    private final ImageIcon image;
    private final ImageType previousType;
    private final ImageType type;
    private final String previousName;
    private final String name;
    private final String previousURL;
    private final String URL;
            

    public ImageUpdateCommand(mlcObject o, ImageIcon image, String name, ImageType type, String URL) {
        super(o, true);
        this.image = image;
        MlcImage si = (MlcImage) o;
        if (!si.getType().equals(MlsImage.ImageType.URL)) {
            Image img = CacheEngineStatic.getImageSync(si.getId());
            if (img != null) {
                previousImage = new ImageIcon(img);
            }
        }
        else {
            previousImage = null;
        }
        
        this.type = type;
        previousType = si.getType();
        previousName = o.getHeadline();
        this.name = name;
        previousURL = si.getUrl();
        this.URL = URL;
        si.setType(type);
        si.setName(name);
        si.setUrl(URL);
        if (image != null) {
            CacheEngineStatic.putImage(o.getId(), image.getImage());
        }
        BulkUpdater.publishUpdate(o);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        try {
            int version;
            ImageManagerRemote imr = (ImageManagerRemote) RemoteLookupAgent.getManagerForClass(ImageManagerRemote.class);
            version = imr.updateImage(getObject().getId(), image, type, name, URL);
            getObject().setVersion(version);
            setExecuted(true);
        } catch (NonExistingObjectException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Image Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        MlcImage img = (MlcImage) getObject();
        CacheEngineStatic.putImage(img.getId(), previousImage.getImage());
        if (isExecuted()) {
            ImageManagerRemote imr = (ImageManagerRemote) RemoteLookupAgent.getManagerForClass(ImageManagerRemote.class);
            if (versionCheck()
                    == false) {
                JOptionPane.showMessageDialog(MindlinerMain.getInstance(),
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Image Update",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    int version;
                    version = imr.updateImage(getObject().getId(), image, previousType, previousName, previousURL);
                    getObject().setVersion(version);
                    setUndone(true);
                } catch (NonExistingObjectException ex) {
                    JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Image Undo Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Image Update (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "Name = " + getObject().getHeadline() + ", x = : " + image.getIconWidth() + ", y = " + image.getIconHeight();
    }
}
