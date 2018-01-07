/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.mindliner.entities.MlsImage;
import com.mindliner.exceptions.InsufficientAccessRightException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.managers.ImageManagerLocal;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.imageio.ImageIO;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * This class returns an image stream to be used as src in an <img> or
 * <p:graphicsImage> tag.
 *
 * @author Marius Messerli
 */
@ManagedBean
@ApplicationScoped
public class ImageRetriever {

    @EJB
    private ImageManagerLocal imageManager;

    public StreamedContent getImage() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // we're rendering the HTML. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        } else {
            // browser is requesting the image. Return a real StreamedContent with the image bytes.
            try {
                String imageIdString = context.getExternalContext().getRequestParameterMap().get("imageId");
                int imageId = Integer.valueOf(imageIdString);
                MlsImage img = imageManager.getImage(imageId);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                BufferedImage bufferedImage = new BufferedImage(img.getPixelSizeX(), img.getPixelSizeY(), 1);
                Graphics gc = bufferedImage.createGraphics();
                boolean success = gc.drawImage(img.getImage().getImage(), 0, 0, null);
                if (success) {
                    ImageIO.write(bufferedImage, "png", bos);
                    ByteArrayInputStream bais = new ByteArrayInputStream(bos.toByteArray());
                    return new DefaultStreamedContent(bais, "image/png");
                }
            } catch (NonExistingObjectException | InsufficientAccessRightException ex) {
                Logger.getLogger(ImageRetriever.class.getName()).log(Level.SEVERE, null, ex);
            }
            return new DefaultStreamedContent();
        }
    }

}
