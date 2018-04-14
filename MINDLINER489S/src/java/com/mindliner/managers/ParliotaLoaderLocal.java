package com.mindliner.managers;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsObject;
import com.mindliner.parliota.objects.ParMeeting;

/**
 *
 * @author Marius Messerli (marius@mindliner.com)
 */
public interface ParliotaLoaderLocal {

    public ParMeeting load(String seed, mlsClient datapool, mlsConfidentiality confidentiality, mlsObject relative);
    
}
