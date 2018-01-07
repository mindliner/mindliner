/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.main.MindlinerMain;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.Date;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class DataPoolUpdateCommand extends MindlinerOnlineCommand {

    private mlcClient dataPool = null;
    private mlcClient previousDataPool = null;
//    private mlsConfidentiality confidentiality = null;
    private mlsConfidentiality previousConfidentiality = null;
    private boolean goodToGo = false;

    /**
     * Like all update commands the constructor also performs the update on the
     * specified object. In addition the modification time of the specified
     * object is updated to now and the default life time is set to the new
     * value.
     *
     * @param o The object for which the lifetime needs to be updated.
     * @param dataPool The new data pool for the object; note that the default
     * confidentiality will be chosen for the object upon data pool update
     */
    public DataPoolUpdateCommand(mlcObject o, mlcClient dataPool) {
        super(o, true);
        assert dataPool != null : "Data pool must not be null";
        if (o.getOwner().equals(CacheEngineStatic.getCurrentUser())) {
            goodToGo = true;
            this.dataPool = dataPool;
            previousDataPool = o.getClient();
            o.setClient(dataPool);
            mlsConfidentiality conf = DefaultObjectAttributes.getConfidentiality(dataPool.getId());
            assert conf != null : "Could not determine default confidentiality";
            previousConfidentiality = o.getConfidentiality();
            o.setConfidentiality(conf);
            o.setModificationDate(new Date());
            DefaultObjectAttributes.updateDataPoolId(dataPool.getId());
            BulkUpdater.publishUpdate(o);
        } else {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), "At least one object belongs to someone else and will not be transfered to the target data pool", "Data Pool Update", JOptionPane.WARNING_MESSAGE);
            goodToGo = false;
        }
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        if (goodToGo) {
            super.execute();
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            int version = omr.setDataPool(getObject().getId(), dataPool.getId(), DefaultObjectAttributes.getConfidentiality(dataPool.getId()).getId());
            getObject().setVersion(version);
            setExecuted(true);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        if (isExecuted()) {
            DefaultObjectAttributes.updateDataPoolId(previousDataPool.getId());
            getObject().setClient(previousDataPool);
            BulkUpdater.publishUpdate(getObject());
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(MindlinerMain.getInstance(),
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Data Pool Update",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                int version = omr.setDataPool(getObject().getId(), previousDataPool.getId(), previousConfidentiality.getId());
                getObject().setVersion(version);
                setUndone(true);
            }
        }
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Data Pool Update (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "new data pool: " + dataPool.getName();
    }

}
