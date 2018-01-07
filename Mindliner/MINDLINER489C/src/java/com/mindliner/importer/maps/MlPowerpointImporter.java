/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.importer.maps;

import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.gui.treemodel.ObjectTreeNode;
import java.io.File;
import java.util.List;

/**
 * Responsible for Import of powerpoint files. It first displays a Dialog for
 * the user to choose which slide to import. Afterwards it displays a second
 * dialog containing the suggested object creation plan.
 * <p>
 * Slide parse algorithm: One object collection for the whole presentation with
 * one child collection for each slide. Each slide is then parsed as follows:
 * for each level 0 list entry a knowlet is created. All list entries with level
 * > 0 will be set as description of the last level 0 knowlet (we don't want to
 * create too many objects).
 *
 * @author Dominic Plangger
 */
public abstract class MlPowerpointImporter<T> implements MlMapImporter {

    protected static final String DEFAULT_HEADLINE = "Imported Powerpoint Presentation";
    protected static final String DEFAULT_SLIDE_HEADLINE = "Slide ";
    protected File file;

    public MlPowerpointImporter(File file) {
        this.file = file;
    }

    public ObjectTreeNode parseSlides(List<T> slides, List<Integer> slideIndices) {
        if (slides == null || slides.isEmpty() || slideIndices == null || slideIndices.isEmpty()) {
            return new ObjectTreeNode(null);
        }

        mlcObjectCollection rootObject = new mlcObjectCollection();
        if (file != null && file.getName() != null && !file.getName().isEmpty()) {
            rootObject.setHeadline(file.getName());
        } else {
            rootObject.setHeadline(DEFAULT_HEADLINE);
        }

        ObjectTreeNode presentationRoot = new ObjectTreeNode(rootObject);

        // Parse each slide
        for (int i = 0; i < slides.size(); i++) {
            T slide = slides.get(i);
            int index = slideIndices.get(i);
            ObjectTreeNode slideRoot = parseSlide(slide, index);
            presentationRoot.add(slideRoot);
        }

        return presentationRoot;
    }

    protected ObjectTreeNode parseSlide(T slide, int index) {
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

    protected abstract void parseSlideContent(T slide, boolean includeSubtitle, ObjectTreeNode slideRoot);

    protected abstract String extractTitle(T titleSlide);

    protected abstract String extractSubtitle(T titleSlide);

    protected abstract String extractContentText(T titleSlide);

}
