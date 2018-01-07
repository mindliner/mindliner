/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.importer.maps;

import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcKnowlet;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.entities.MlsImage;
import com.mindliner.exceptions.ImportException;
import com.mindliner.gui.treemodel.ImageTreeNode;
import com.mindliner.gui.treemodel.ObjectTreeNode;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.poi.xslf.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import static org.apache.poi.xslf.usermodel.XSLFPictureData.PICTURE_TYPE_BMP;
import static org.apache.poi.xslf.usermodel.XSLFPictureData.PICTURE_TYPE_JPEG;
import static org.apache.poi.xslf.usermodel.XSLFPictureData.PICTURE_TYPE_PNG;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

/**
 * Importer for PPT 2007+ files (xml based file format)
 *
 * @author Dominic Plangger
 */
public class MlPowerpointXmlImporter extends MlPowerpointImporter<XSLFSlide> {

    public MlPowerpointXmlImporter(File file) {
        super(file);
    }

    @Override
    public mlcObject importMap(File f) throws ImportException {

        PowerpointImportWorker worker = new PowerpointImportWorker(this, file, true);
        worker.execute();

        return null;
    }

    @Override
    protected String extractTitle(XSLFSlide slide) {
        if (slide.getTitle() != null && !slide.getTitle().isEmpty()) {
            return slide.getTitle().trim();
        }
        for (XSLFShape shape : slide) {
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape txShape = (XSLFTextShape) shape;
                if (Placeholder.TITLE.equals(txShape.getTextType())
                        || Placeholder.CENTERED_TITLE.equals(txShape.getTextType())) {
                    return txShape.getText().trim();
                }
            }
        }
        return null;
    }

    @Override
    protected String extractSubtitle(XSLFSlide slide) {
        for (XSLFShape shape : slide) {
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape txShape = (XSLFTextShape) shape;
                if (Placeholder.SUBTITLE.equals(txShape.getTextType())) {
                    return txShape.getText().trim();
                }
            }
        }
        return null;
    }

    @Override
    protected String extractContentText(XSLFSlide slide) {
        StringBuilder sb = new StringBuilder();
        for (XSLFShape shape : slide) {
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape txShape = (XSLFTextShape) shape;
                if (!Placeholder.TITLE.equals(txShape.getTextType())
                        && !Placeholder.CENTERED_TITLE.equals(txShape.getTextType())
                        && !Placeholder.SUBTITLE.equals(txShape.getTextType())
                        && !Placeholder.SLIDE_NUMBER.equals(txShape.getTextType())) {
                    sb.append(txShape.getText());
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    @Override
    protected ObjectTreeNode parseSlide(XSLFSlide slide, int index) {
        mlcObjectCollection rootObject = new mlcObjectCollection();
        ObjectTreeNode slideRoot = new ObjectTreeNode(rootObject);
        boolean includeSubtitle = true;
        boolean hasDefaultHeadline = false;

        // parse slide title for slide root object
        String title = extractTitle(slide);
        if (title == null || title.isEmpty()) {
            includeSubtitle = false;
            String subtitle = extractSubtitle(slide);
            if (subtitle == null || subtitle.isEmpty()) {
                rootObject.setHeadline(DEFAULT_SLIDE_HEADLINE + Integer.toString(index + 1));
                hasDefaultHeadline = true;
            } else {
                rootObject.setHeadline(subtitle);
            }
        } else {
            rootObject.setHeadline(title);
        }

        if (!hasDefaultHeadline) {
            rootObject.setDescription(DEFAULT_SLIDE_HEADLINE + Integer.toString(index + 1));
        }
        parseSlideContent(slide, includeSubtitle, slideRoot);

        return slideRoot;
    }

    @Override
    protected void parseSlideContent(XSLFSlide slide, boolean includeSubtitle, ObjectTreeNode slideRoot) {
        for (XSLFShape shape : slide) {
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape txShape = (XSLFTextShape) shape;
                if (!Placeholder.TITLE.equals(txShape.getTextType())
                        && !Placeholder.CENTERED_TITLE.equals(txShape.getTextType())
                        && !Placeholder.SLIDE_NUMBER.equals(txShape.getTextType())
                        && (!Placeholder.SUBTITLE.equals(txShape.getTextType()) || includeSubtitle)) {

                    Map<Integer, ObjectTreeNode> lastObjects = new HashMap<>();
                    lastObjects.put(-1, slideRoot);

                    for (XSLFTextParagraph p : txShape.getTextParagraphs()) {
                        if (p.getText().trim().isEmpty()) {
                            continue;
                        }

                        mlcKnowlet k = new mlcKnowlet();
                        k.setHeadline(p.getText().trim());
                        ObjectTreeNode node = new ObjectTreeNode(k);

                        int level = p.getLevel();
                        ObjectTreeNode currRoot;
                        do {
                            currRoot = lastObjects.get(--level);
                        } while (currRoot == null && level >= 0);
                        if (currRoot == null) {
                            currRoot = slideRoot;
                        }
                        currRoot.add(node);
                        lastObjects.put(p.getLevel(), node);
                        dropBiggerEntries(p.getLevel(), lastObjects);
                    }
                }
            }
            else if (shape instanceof XSLFPictureShape) {
                // Pictures may be uploaded to cloud storage, therefore save picture data in the object tree
                XSLFPictureShape pShape = (XSLFPictureShape) shape;
                XSLFPictureData data = pShape.getPictureData();
                byte[] bytes = data.getData();
                String mime = resolveMime(data.getPictureType());
                MlcImage image = new MlcImage();
                image.setType(MlsImage.ImageType.URL);
                image.setHeadline(data.getFileName());
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
            case PICTURE_TYPE_JPEG:
                return "image/jpeg";
            case PICTURE_TYPE_PNG:
                return "image/png";
            case PICTURE_TYPE_BMP:
                return "image/bmp";
            default:
                return null;
        }
    }

}
