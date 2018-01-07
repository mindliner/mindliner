/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands.bulk;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.clientobjects.ObjectIdLister;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class DataPoolBulkUpdateCommand extends BulkUpdateCommand {

    mlcClient dataPool = null;
    Map<Integer, mlcClient> previoudDataPools;
    int previousDataPoolDefaultId;
    Map<Integer, mlsConfidentiality> previousConfidentialities;

    public DataPoolBulkUpdateCommand(List<mlcObject> candidates, mlcClient dataPool) {
        super(candidates);
        boolean rejectedCandidates = false;
        for (Iterator it = candidates.iterator(); it.hasNext();) {
            mlcObject c = (mlcObject) it.next();
            if (!CacheEngineStatic.getCurrentUser().equals(c.getOwner())) {
                rejectedCandidates = true;
                it.remove();
            }
        }
        if (rejectedCandidates) {
            JOptionPane.showMessageDialog(null, "Objects belonging to someone else will be transfered to the target data pool", "Data Pool Update", JOptionPane.WARNING_MESSAGE);
        }
        this.dataPool = dataPool;
        previoudDataPools = new HashMap<>();
        previousConfidentialities = new HashMap<>();
        for (mlcObject o : objects) {
            previoudDataPools.put(o.getId(), o.getClient());
            previousConfidentialities.put(o.getId(), o.getConfidentiality());
            o.setClient(dataPool);
            o.setConfidentiality(DefaultObjectAttributes.getConfidentiality(dataPool.getId()));
        }
        previousDataPoolDefaultId = DefaultObjectAttributes.getDataPoolId();
        DefaultObjectAttributes.updateDataPoolId(dataPool.getId());
        BulkUpdater.publishUpdates(objects);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        mlsConfidentiality conf = DefaultObjectAttributes.getConfidentiality(dataPool.getId());
        if (conf == null) {
            JOptionPane.showMessageDialog(null, "Could not determine default confidentiality", "Data Pool Update", JOptionPane.ERROR_MESSAGE);
        }
        Map<Integer, Integer> versions = omr.bulkSetDataPool(ObjectIdLister.getIdList(objects), dataPool.getId(), conf.getId());
        updateObjectVersions(versions);
        setExecuted(true);
    }

    /**
     * The undo operation has to be executed on each object individually as
     * their previous data pools may have been different from one another.
     *
     * @throws mlModifiedException Thrown if object was modified in the
     * meantime.
     * @throws NamingException Thrown if the remote enterprise bean cannot be
     * found.
     * @throws UndoAlreadyUndoneException Thrown if this command was undone
     * before.
     */
    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        for (mlcObject o : getObjects()) {
            mlcClient previousDataPool = previoudDataPools.get(o.getId());
            if (previousDataPool != null) {
                o.setClient(previousDataPool);
            }
        }
        BulkUpdater.publishUpdates(objects);
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(null,
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Bulk Data Pool Update",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                for (mlcObject o : getObjects()) {
                    mlcClient previousDataPool = previoudDataPools.get(o.getId());
                    mlsConfidentiality previousConf = previousConfidentialities.get(previousDataPool.getId());
                    if (previousDataPool != null && previousConf != null) {
                        int version = omr.setDataPool(o.getId(), previousDataPool.getId(), previousConf.getId());
                        o.setVersion(version);
                    }
                }
            }
        }
        setUndone(true);
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "bulk update data pool to " + dataPool;
    }

}
