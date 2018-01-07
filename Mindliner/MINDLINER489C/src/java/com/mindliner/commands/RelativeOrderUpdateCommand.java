/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.managers.LinkManagerRemote;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;

/**
 * Updates the position of the specified relative of the specified parent
 *
 * @author Marius Messerli
 */
public class RelativeOrderUpdateCommand extends MindlinerOnlineCommand {

    private final int position;
    private final int relativeId;
    private final int previousposition;
    private final boolean previousParentRelativesOrdered;

    public RelativeOrderUpdateCommand(mlcObject parent, mlcObject relative, int newPosition) {
        super(parent, false);
        this.position = newPosition;
        this.relativeId = relative.getId();
        previousParentRelativesOrdered = parent.isRelativesOrdered();
        parent.setRelativesOrdered(true);
        MlcLink link = CacheEngineStatic.getLink(parent.getId(), relative.getId());
        if (link != null) {
            previousposition = link.getRelativeListPosition();
            link.setRelativeListPosition(newPosition);
        } else {
            previousposition = 0;
        }
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        LinkManagerRemote l = (LinkManagerRemote) RemoteLookupAgent.getManagerForClass(LinkManagerRemote.class);
        int version = l.setRelativePosition(getObject().getId(), relativeId, position);
        getObject().setVersion(version);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        if (isExecuted()) {
            LinkManagerRemote l = (LinkManagerRemote) RemoteLookupAgent.getManagerForClass(LinkManagerRemote.class);
            int version = l.setRelativePosition(getObject().getId(), relativeId, previousposition);
            if (!previousParentRelativesOrdered) {
                ObjectManagerRemote orm = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
                version = orm.setRelativesOrdered(getObject().getId(), previousParentRelativesOrdered);
            }
            getObject().setVersion(version);
        }
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Relative Order Position (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "new position: " + position;
    }

}
