package com.mindliner.gui.tablemodels;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlClientClassHandler;
import com.mindliner.clientobjects.mlcContact;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.img.icons.MlIconManager;
import com.mindliner.img.tools.ImageResizer;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 *
 * @author Marius Messerli
 */
public class AllObjectTableRow extends MlTableRow {

    public AllObjectTableRow(mlcObject o, int cols) {
        super(o, cols);
    }

    @Override
    public int addSpecificColumns(int currCol) {
        currCol = addHeadlineColumn(currCol);
        if (numCols > currCol) {
            // the icon column
            boolean useTypeIcon = true;
            if (sourceObject instanceof mlcContact) {
                mlcContact c = (mlcContact) sourceObject;
                if (c.getProfilePicture() != null) {
                    Image image = CacheEngineStatic.getImageSync(c.getProfilePicture().getId());
                    if (image != null) {
                        BufferedImage thumbnailImage = ImageResizer.resize(image, 32, 32);
                        cellObjects[currCol++] = thumbnailImage;
                        useTypeIcon = false;
                    }
                }
            }
            if (useTypeIcon) {
                boolean completed = false;
                if (sourceObject instanceof mlcTask) {
                    completed = ((mlcTask) sourceObject).isCompleted();
                }
                cellObjects[currCol++] = MlIconManager.getIconForType(MlClientClassHandler.getTypeByClass(sourceObject.getClass()), completed).getImage();
            }
        }
        return currCol;
    }
}
