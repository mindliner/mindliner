/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.gui.color;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcObject;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Colorizes objects based on their client attribute.
 * 
 * @author Marius Messerli
 */
public class DataPoolColorizer extends BaseColorizer<mlcClient> {

    public DataPoolColorizer(Color defaultColor) {
        super(defaultColor);
    }

    @Override
    public Color getColorForObject(Object o) {
        if (!(o instanceof mlcObject)) {
            throw new IllegalArgumentException("The argument must be instance of mlcObject");
        }

        mlcObject mo = (mlcObject) o;
        if (colorMap == null) {
            return getDefaultColor();
        }
        Color c = (Color) colorMap.get(mo.getClient());
        if (c == null) {
            return getDefaultColor();
        }
        return c;
    }
    
    /**
     * This method uses the link owner instead of the object owner to determine the color.
     * 
     * @param link The link for which the color is requested
     * @return 
     */
    @Override
    public Color getColorForLink(MlcLink link){
        if (colorMap == null) {
            return getDefaultColor();
        }
        Color c = (Color) colorMap.get(link.getClient());
        if (c == null) {
            return getDefaultColor();
        }
        return c;        
    }

    @Override
    public int getKeyId(mlcClient key) {
        return key.getId();
    }

    @Override
    protected String getSpecificFileNameElement() {
        return MlClassHandler.getClassNameOnly(getClass().getName());
    }

    @Override
    public List<mlcClient> getInputValueList() {
        
        List<mlcClient> clients = new ArrayList<>();
        for (Integer i : CacheEngineStatic.getCurrentUser().getClientIds()){
            mlcClient client = CacheEngineStatic.getClient(i);
            assert client != null : "Data pool missing in cache";
            clients.add(client);
        }
        return clients;
    }    
    
}
