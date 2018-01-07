/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.mindliner.entities.MlsImage;
import com.mindliner.entities.mlsClient;
import com.mindliner.managers.ImageManagerLocal;
import com.mindliner.managers.ObjectManagerLocal;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.ws.rs.Path;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 * This backing bean manages images and icons. Icons as seen by the Mindliner
 * user are actually just images which is why they are coded here.
 *
 * @author Marius Messerli
 */
@ManagedBean
@SessionScoped
public class ImageBB {

    private List<UploadedFile> uploadedFiles;
    private static final int maxIconSize = 256 * 256;

    private List<MlsImage> icons = null;
    private final Map<Integer, Boolean> iconSelection = new HashMap<>();
    private final Map<Integer, String> previousDescriptions = new HashMap<>();

    @EJB
    private ImageManagerLocal imageManager;

    @EJB
    private ObjectManagerLocal objectManager;

    @PostConstruct
    public void init() {
        uploadedFiles = new ArrayList<>();
    }

    public int getIconCount(mlsClient client) {
        return getIcons(client).size();
    }

    public void deleteAllIcons(mlsClient client) {
        imageManager.deleteAllIcons(client);
        icons = null;
    }

    public void upload(FileUploadEvent event) {
        uploadedFiles.add(event.getFile());
    }

    public void saveUploadedImages(mlsClient client) {
        try {
            for (UploadedFile f : uploadedFiles) {
                List<ImageIcon> tmpIcons = new ArrayList<>();
                ImageIcon newImage = new ImageIcon();
                newImage.setDescription(f.getFileName());

                byte[] buffer = new byte[maxIconSize];

                int bytesRead = f.getInputstream().read(buffer);
                if (bytesRead >= maxIconSize) {
                    FacesMessage message = new FacesMessage();
                    message.setSeverity(FacesMessage.SEVERITY_ERROR);
                    message.setSummary("The icon must not be larger than 128 x 128 pixels");
                    FacesContext.getCurrentInstance().addMessage("dataForm:uploadId", message);
                    return;
                }
                Image img = ImageIO.read(new ByteArrayInputStream(buffer));
                newImage.setImage(img);
                tmpIcons.add(newImage);
                imageManager.addIconImages(client, tmpIcons);
                System.out.println("saved " + f.getFileName());
            }
            // force reload
            icons = null;
        } catch (IOException ex) {
            FacesMessage message = new FacesMessage();
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            message.setSummary("File input error");
            message.setDetail(ex.getMessage());
            FacesContext.getCurrentInstance().addMessage("dataForm:uploadId", message);
        }
    }

    /**
     * Returns a list of icons belonging to the specified client
     *
     * @param client
     * @return
     */
    public List<MlsImage> getIcons(mlsClient client) {
        if (icons == null || icons.isEmpty() || !client.equals(icons.get(0).getClient())) {
            icons = imageManager.getIcons(client);
            iconSelection.clear();
            previousDescriptions.clear();
            for (MlsImage icon : icons){
                previousDescriptions.put(icon.getId(), icon.getDescription());
            }
        }
        return icons;
    }

    public int getMaxIconSize() {
        return maxIconSize;
    }

    public Map<Integer, Boolean> getIconSelection() {
        return iconSelection;
    }

    public boolean isOneSelected() {
        for (Integer i : iconSelection.keySet()) {
            if (iconSelection.get(i)) {
                return true;
            }
        }
        return false;
    }

    public void selectAll() {
        iconSelection.clear();
        for (MlsImage i : icons) {
            iconSelection.put(i.getId(), true);
        }
    }

    public void ensureWSMIcons(mlsClient dataPool) {
        imageManager.ensureDefaultWSMIconSet(dataPool.getId());
    }

    public void deselectAll() {
        iconSelection.clear();
    }
    
    public void saveDescriptions(){
        for (MlsImage icon : icons){
            String orig = previousDescriptions.get(icon.getId());
            if (orig != null && !orig.equals(icon.getDescription())){
                System.out.println("updating description for id " + icon.getId() + " to " + icon.getDescription());
                objectManager.setDescription(icon.getId(), icon.getDescription());
                // update map of previous descriptions
                previousDescriptions.put(icon.getId(), icon.getDescription());
            }
        }
    }

    public void deleteSelectedIcons() {
        List<Integer> selectedIds = new ArrayList<>();
        for (Integer i : iconSelection.keySet()) {
            if (iconSelection.get(i)) {
                selectedIds.add(i);
            }
        }
        objectManager.removeObjects(selectedIds);
        icons = null;
    }

}
