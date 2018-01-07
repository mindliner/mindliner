/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exporter;

import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.exceptions.ExportException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.xslf.usermodel.SlideLayout;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import com.mindliner.clientobjects.MlMapNode;

/**
 * Exporter for Powerpoint presentations (*.pptx)
 *
 * @author Dominic Plangger
 */
public class PowerpointExporter implements MlExporter {

    /**
     *
     * @param includeDescr whether or not the object descriptions should be
     * included in the PPT presentation
     * @param maxDescrLength value 0: all description accepted. value > 0:
     * description will be truncated
     * @param bulletsPerSlide how many level 0 bullets should be on one slide
     * (number of higher level bullets depends on this value)
     * @param bulletsForDescr whether or not a bullet should be created for
     * description paragraphs
     */
    public PowerpointExporter(boolean includeDescr, long maxDescrLength, long bulletsPerSlide, boolean bulletsForDescr) {
        this.includeDescr = includeDescr;
        this.maxDescrLength = maxDescrLength;
        this.bulletsPerSlide = bulletsPerSlide;
        this.bulletsForDescr = bulletsForDescr;
    }

    private boolean includeDescr;
    private boolean bulletsForDescr;
    private long maxDescrLength;
    private long bulletsPerSlide;

    private static final int MAIN_FONT_SIZE = 32;
    private static final int DESCRIPTION_FONT_SIZE = 12;
    private static final String DEFAULT_SLIDE_TITLE = "General Information";

    @Override
    public void export(List<MlMapNode> rootNodes, File file) throws ExportException {
        try {
            XMLSlideShow ppt = new XMLSlideShow();
            XSLFSlideMaster master = ppt.getSlideMasters()[0];
            XSLFSlideLayout titleLayout = master.getLayout(SlideLayout.TITLE);

            // use headline of root node as slide title, and description as
            // slide subtitle
            for (MlMapNode root : rootNodes) {
                XSLFSlide titleSlide = ppt.createSlide(titleLayout);
                XSLFTextShape title = titleSlide.getPlaceholder(0);
                title.setText(root.getObject().getHeadline());
                XSLFTextShape subtitle = titleSlide.getPlaceholder(1);
                subtitle.setText(root.getObject().getDescription());
                createContentSlides(root, ppt);
            }

            FileOutputStream out = new FileOutputStream(file);
            ppt.write(out);
            out.close();
        } catch (Exception ex) {
            Logger.getLogger(PowerpointExporter.class.getName()).log(Level.SEVERE, null, ex);
            throw new ExportException(ex);
        }
    }

    private void createContentSlides(MlMapNode root, XMLSlideShow ppt) {
        List<Integer> indents = new ArrayList<Integer>();
        List<MlMapNode> serializedTree = new ArrayList<MlMapNode>();
        List<MlMapNode> objCollections = new ArrayList<MlMapNode>();
        for (MlMapNode n : root.getChildren()) {
            if (!(n.getObject() instanceof mlcObjectCollection)) {
                serializeTree(n, serializedTree, indents, 0);
            } else {
                objCollections.add(n);
            }
        }

        // All children except collections are added under a general information slide.
        // If slide is full, a '... ctd' slide will be created
        createSlides(ppt, serializedTree, indents, DEFAULT_SLIDE_TITLE);

        // For immediate children that are collections, create an own slide and use the headline
        // of the collection as slide title
        for (MlMapNode coll : objCollections) {
            serializedTree.clear();
            indents.clear();
            serializeTree(coll, serializedTree, indents, 0);
            serializedTree.remove(0);
            indents.remove(0);
            createSlides(ppt, serializedTree, indents, coll.getObject().getHeadline());
        }

    }

    private void serializeTree(MlMapNode root, List<MlMapNode> serializedTree, List<Integer> indents, int currIndent) {
        serializedTree.add(root);
        indents.add(currIndent);
        for (MlMapNode child : root.getChildren()) {
            serializeTree(child, serializedTree, indents, currIndent + 1);
        }
    }

    private void createSlides(XMLSlideShow ppt, List<MlMapNode> serializedTree, List<Integer> indents, String slideHeadline) {
        double sum = 0;
        XSLFTextShape content = null;
        XSLFSlideMaster master = ppt.getSlideMasters()[0];
        XSLFSlideLayout normalLayout = master.getLayout(SlideLayout.TITLE_AND_CONTENT);
        for (int i = 0; i < serializedTree.size(); i++) {
            // font size varies with the paragraph indent level.
            // we can put more text on the slide if paragraph level is smaller
            if (sum == 0 || sum >= bulletsPerSlide * MAIN_FONT_SIZE) {
                sum = 0;
                XSLFSlide slide = ppt.createSlide(normalLayout);
                XSLFTextShape title = slide.getPlaceholder(0);
                title.setText(slideHeadline + (i == 0 ? "" : " ctd."));
                content = slide.getPlaceholder(1);
                content.clearText();
            }
            if (content == null) {
                continue;
            }
            XSLFTextParagraph paragraph = content.addNewTextParagraph();
            paragraph.setLevel(indents.get(i));
            XSLFTextRun textRun = paragraph.addNewTextRun();
            textRun.setText(serializedTree.get(i).getObject().getHeadline());
            sum += textRun.getFontSize();

            // append description if desired 
            if (includeDescr) {
                String descr = serializedTree.get(i).getObject().getDescription();
                if (descr != null && !descr.isEmpty()) {
                    paragraph = content.addNewTextParagraph();
                    paragraph.setLevel(indents.get(i) + 1);
                    paragraph.setBullet(bulletsForDescr);
                    textRun = paragraph.addNewTextRun();
                    if (maxDescrLength > 0 && maxDescrLength < descr.length()) {
                        descr = descr.substring(0, (int) maxDescrLength) + "...";
                    }
                    descr = descr.replace("\n", " ").replace("\r", " ");
                    textRun.setText(descr);
                    textRun.setFontSize(DESCRIPTION_FONT_SIZE);
                    sum += textRun.getFontSize();
                }
            }
        }
    }
}
