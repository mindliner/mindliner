/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.cache;

import com.mindliner.categories.*;
import java.util.List;

/**
 *
 * @author M.Messerli
 */
public interface CategoryCache {

    mlsConfidentiality getConfidentiality(int id);

    /**
     * Returns the closest confidentiality to the specified level
     *
     * @param level The numeric confidentiality level.
     * @return
     */
    mlsConfidentiality getClosestConfidentialityForLevel(int level);

    MlsNewsType getActionItemType(int id);

    mlsPriority getPriority(int id);

    mlsPriority getPriority(String name);

    List<mlsPriority> getPriorities();

    public List<mlsConfidentiality> getConfidentialities();
    
    public List<mlsConfidentiality> getConfidentialities(int clientId);

    public List<MlsNewsType> getActionItemTypes();

}
