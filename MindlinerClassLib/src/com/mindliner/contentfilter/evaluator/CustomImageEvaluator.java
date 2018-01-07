/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.contentfilter.evaluator;

import com.mindliner.contentfilter.ObjectEvaluator;
import com.mindliner.entities.MlsImage;
import com.mindliner.entities.mlsObject;
import java.io.Serializable;

/**
 * This evaluator ensures that only custom and url images (no icons and no profile pictures)
 * are browsable in ordinary object searches.
 *
 * @author Marius Messerli
 */
public class CustomImageEvaluator implements ObjectEvaluator, Serializable {

    private static final long serialVersionUID = 19640205L;

    @Override
    public boolean passesEvaluation(mlsObject o) {
        if (o instanceof MlsImage) {
            MlsImage s = (MlsImage) o;
            if (s.getType() != MlsImage.ImageType.Custom && s.getType() != MlsImage.ImageType.URL) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isMultipleInstancesAllowed() {
        return false;
    }
}
