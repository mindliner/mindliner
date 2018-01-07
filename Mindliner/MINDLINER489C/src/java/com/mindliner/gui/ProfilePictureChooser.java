/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcContact;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.ContactProfilePictureUpdateCommand;
import com.mindliner.commands.ImageUpdateCommand;
import com.mindliner.commands.ObjectCreationCommand;
import com.mindliner.commands.ObjectDeletionCommand;
import com.mindliner.entities.MlsImage;
import com.mindliner.main.MindlinerMain;
import com.mindliner.prefs.FileLocationPreferences;
import java.awt.Image;
import java.awt.MediaTracker;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * This class lets the user choose a new profile picture for a contact.
 *
 * @author Marius Messerli
 */
public class ProfilePictureChooser {

    private static File profilePictureCurentDirecotry = null;
    private static final String PROFILE_PICTURE_DIRECTORY_KEY = "profilepicturelocation";

    public static Image changeProfilePicture(mlcContact contact) {
        if (contact != null) {
            CommandRecorder cr = CommandRecorder.getInstance();
            final JFileChooser fc = new JFileChooser(FileLocationPreferences.getLocation(PROFILE_PICTURE_DIRECTORY_KEY));
            fc.setDialogTitle("Select Image File");
            fc.setCurrentDirectory(profilePictureCurentDirecotry);
            int returnVal = fc.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                FileLocationPreferences.setLocation(PROFILE_PICTURE_DIRECTORY_KEY, fc.getSelectedFile().getParent());
                profilePictureCurentDirecotry = fc.getSelectedFile().getParentFile();
                ImageIcon newImage = new ImageIcon(fc.getSelectedFile().getAbsolutePath());
                if (newImage.getIconHeight() > 512 || newImage.getIconWidth() > 512) {
                    JOptionPane.showMessageDialog(null, "Please choose an image smaller than 512 pixels wide and high", "Profile Picture", JOptionPane.ERROR_MESSAGE);
                } else {
                    // ensure image fully loaded
                    try {
                        while (newImage.getImageLoadStatus() == MediaTracker.LOADING) {
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ProfilePictureChooser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (newImage.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    // delete any previous profile picture for contact
                    if (contact.getProfilePicture() != null) {
                        cr.scheduleCommand(new ObjectDeletionCommand(contact.getProfilePicture()));
                    }
                    ObjectCreationCommand imageCreationCommand = new ObjectCreationCommand(null, MlcImage.class, "", "");
                    cr.scheduleCommand(imageCreationCommand);
                    MlcImage img = (MlcImage) imageCreationCommand.getObject();
                    if (img != null) {
                        cr.scheduleCommand(new ImageUpdateCommand(img, newImage, "Profile picture for " + contact.getName(), MlsImage.ImageType.ProfilePicture, null));
                    }
                    MlcImage profilePicture = (MlcImage) CacheEngineStatic.getObject(imageCreationCommand.getObject().getId());
                    cr.scheduleCommand(new ContactProfilePictureUpdateCommand(contact, profilePicture));
                    return CacheEngineStatic.getImageSync(profilePicture.getId());
                } else {
                    System.err.println("failed to load profile picture " + newImage.getDescription());
                    return null;
                }
            }
        }
        return null;
    }

    public static void deleteProfilePicture(mlcContact contact) {
        CommandRecorder cr = CommandRecorder.getInstance();
        if (contact.getProfilePicture() != null) {
            int answer = JOptionPane.showConfirmDialog(MindlinerMain.getInstance(), "OK to delete current profile picture?", "Profile Picture", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                // delete the picture
                cr.scheduleCommand(new ObjectDeletionCommand(contact.getProfilePicture()));
                // set null profile picture, re-loading currentContact from cache to ensure it is updated after above command
                cr.scheduleCommand(new ContactProfilePictureUpdateCommand(CacheEngineStatic.getObject(contact.getId()), null));
            }
        }
    }
}
