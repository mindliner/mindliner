/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.importer.maps;

import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcKnowlet;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.MlsImage;
import com.mindliner.exceptions.ImportException;
import com.mindliner.gui.treemodel.ImageTreeNode;
import com.mindliner.gui.treemodel.ObjectTreeNode;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.poi.hslf.model.Picture;
import static org.apache.poi.hslf.model.Picture.DIB;
import static org.apache.poi.hslf.model.Picture.JPEG;
import static org.apache.poi.hslf.model.Picture.PNG;
import static org.apache.poi.hslf.model.Picture.EMF;
import org.apache.poi.hslf.model.Shape;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextShape;
import org.apache.poi.hslf.record.OEPlaceholderAtom;
import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hslf.usermodel.RichTextRun;

/**
 *
 * Importer for PPT 97-2003 files
 * @author Dominic Plangger
 */
public class MlPowerpointOldImporter extends MlPowerpointImporter<Slide> {
    
    public MlPowerpointOldImporter(File file) {
        super(file);
    }
    
    @Override
    public mlcObject importMap(File file) throws ImportException {

        PowerpointImportWorker worker = new PowerpointImportWorker(this, file, false);
        worker.execute();
        
        return null;
    }


    @Override
    protected String extractTitle(Slide slide) {
        if (slide.getTitle() != null && !slide.getTitle().isEmpty()) {
            return slide.getTitle().trim();
        }
        for (Shape shape : slide.getShapes()) {
            if (shape instanceof TextShape) {
                TextShape txShape = (TextShape) shape;
                if (txShape.getPlaceholderAtom() == null) {
                    // for special text boxes, the placeholder is null.
                    // treat them as normal content (not as title/subtitle)
                    continue;
                }
                if (txShape.getPlaceholderAtom().getPlaceholderId() == OEPlaceholderAtom.Title 
                        || txShape.getPlaceholderAtom().getPlaceholderId() == OEPlaceholderAtom.CenteredTitle) {
                    return txShape.getText() != null ? txShape.getText().trim() : null;
                }
            }
        }
        return null;
    }
    
    @Override
    protected String extractSubtitle(Slide slide) {
        for (Shape shape : slide.getShapes()) {
            if (shape instanceof TextShape) {
                TextShape txShape = (TextShape) shape;
                if (txShape.getPlaceholderAtom() == null) {
                    continue;
                }
                if (txShape.getPlaceholderAtom().getPlaceholderId() == OEPlaceholderAtom.Subtitle) {
                    return txShape.getText().trim();
                }
            }
        }
        return null;
    }

    
    @Override
    protected String extractContentText(Slide slide) {
        StringBuilder sb = new StringBuilder();
        for (Shape shape : slide.getShapes()) {
            if (shape instanceof TextShape) {
                TextShape txShape = (TextShape) shape;
                // for special text boxes, the placeholder is null.
                // treat them as normal content (not as title/subtitle)
                if (txShape.getPlaceholderAtom() == null || 
                        (txShape.getPlaceholderAtom().getPlaceholderId() != OEPlaceholderAtom.Title
                        && txShape.getPlaceholderAtom().getPlaceholderId() != OEPlaceholderAtom.CenteredTitle
                        && txShape.getPlaceholderAtom().getPlaceholderId() != OEPlaceholderAtom.Subtitle)) {
                    sb.append(txShape.getText());
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    @Override
    protected void parseSlideContent(Slide slide, boolean includeSubtitle, ObjectTreeNode slideRoot) {
        for (Shape shape : slide.getShapes()) {
            if (shape instanceof TextShape) {
                TextShape txShape = (TextShape) shape;
                if (txShape.getPlaceholderAtom() == null || 
                        (txShape.getPlaceholderAtom().getPlaceholderId() != OEPlaceholderAtom.Title
                        && txShape.getPlaceholderAtom().getPlaceholderId() != OEPlaceholderAtom.CenteredTitle
                        && (txShape.getPlaceholderAtom().getPlaceholderId() != OEPlaceholderAtom.Subtitle || includeSubtitle))) {
                    
                    Map<Integer, ObjectTreeNode> lastObjects = new HashMap<>();
                    lastObjects.put(0, slideRoot);
                    
                    if (txShape.getTextRun() == null) {
                        continue;
                    }
                    for (RichTextRun r : txShape.getTextRun().getRichTextRuns()) {
                        if (r.getText().trim().isEmpty() || r.getText().trim().equals("*")) { // @todo figure out why apache poi creates a '*' text for each slide
                            continue;
                        }
                        mlcKnowlet k = new mlcKnowlet();
                        k.setHeadline(r.getText().trim());
                        ObjectTreeNode node = new ObjectTreeNode(k);

                        int level = r.getIndentLevel();
                        ObjectTreeNode currRoot;
                        do {
                            currRoot = lastObjects.get(--level);
                        } while (currRoot == null && level >= 0);
                        if (currRoot == null) {
                            currRoot = slideRoot;
                        }
                        currRoot.add(node);
                        lastObjects.put(r.getIndentLevel(), node);
                        dropBiggerEntries(r.getIndentLevel(), lastObjects);
                    }
                }
            }
            else if (shape instanceof Picture) {
                // Pictures may be uploaded to cloud storage, therefore save picture data in the object tree
                Picture pictureShape = (Picture) shape;
                PictureData data = pictureShape.getPictureData();
                byte[] bytes = data.getData();
                String mime = resolveMime(data.getType());
                MlcImage image = new MlcImage();
                image.setType(MlsImage.ImageType.URL);
                image.setHeadline(pictureShape.getPictureName());
                ImageTreeNode node = new ImageTreeNode(bytes, mime, image);
                slideRoot.add(node);
            }
        }
    }
    
    private void dropBiggerEntries(int max, Map<Integer, ObjectTreeNode> lastObjects) {
        Iterator<Integer> it = lastObjects.keySet().iterator();
        while (it.hasNext()) {
            if (it.next() > max) {
                it.remove();
            }
        }
    }

    private String resolveMime(int pictureType) {
        switch (pictureType) {
            case JPEG:
                return "image/jpeg";
            case PNG:
                return "image/png";
            case DIB:
                return "image/bmp";
            case EMF:
                return "image/x-emf";
            default:
                return null; 
        }
    }

}
