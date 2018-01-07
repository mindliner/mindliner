/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.MlLinkException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.MindlinerMain;
import com.mindliner.managers.LinkManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * This command removes a link first from the cache and when online from the
 * server.
 *
 * @author Marius Messerli
 */
public class UnlinkCommand extends MindlinerOnlineCommand {

    private mlcObject object2 = null;
    private boolean isOneWay;
    private LinkRelativeType type;
    private boolean proceed = true;

    public UnlinkCommand(mlcObject o, mlcObject o2, boolean isOneWay) {
        this(o, o2, isOneWay, LinkRelativeType.OBJECT);
    }

    public UnlinkCommand(mlcObject o, mlcObject o2, boolean isOneWay, LinkRelativeType linkType) {
        super(o, true);
        if (o2 == null) {
            throw new IllegalArgumentException("Object 2 must not be null.");
        }
        this.isOneWay = isOneWay;
        this.type = linkType;
        this.object2 = o2;
        if (LinkRelativeType.OBJECT.equals(linkType)) {
            MlcLink l = CacheEngineStatic.getLink(o.getId(), o2.getId());
            if (l == null) {
                // if a child node hasn't been expanded yet, there might be only the link object from parent to child in the cache
                // as we don't know whether o or o2 is the parent, we need to check both
                l = CacheEngineStatic.getLink(o2.getId(), o.getId());
            }
            if (l != null) {
                if (!CacheEngineStatic.getCurrentUser().equals(l.getOwner())) {
                    int ans = JOptionPane.showConfirmDialog(MindlinerMain.getInstance(), "Link belongs to " + l.getOwner().getFirstName() + ". Do you want to remove/re-link it anyway?", "Link Removal", JOptionPane.YES_NO_OPTION);
                    if (ans == JOptionPane.NO_OPTION) {
                        proceed = false;
                        return;
                    }
                }
                CacheEngineStatic.removeRelative(o, o2, isOneWay);
                LinkCommand.updateNonCellRelatives(o, o2, false, isOneWay);
            }
        }
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        if (!proceed) {
            return;
        }
        super.execute();
        mapTemporaryObjectId(object2);
        LinkManagerRemote linker = (LinkManagerRemote) RemoteLookupAgent.getManagerForClass(LinkManagerRemote.class);
        int[] vers = linker.unlink(getObject().getId(), object2.getId(), isOneWay, type);
        if (vers.length == 2) {
            getObject().setVersion(vers[0]);
            if (!isOneWay) {
                object2.setVersion(vers[1]);
            }
        }
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        if (!proceed) {
            return;
        }
        super.undo();
        CacheEngineStatic.addRelative(getObject(), object2, isOneWay);
        LinkCommand.updateNonCellRelatives(getObject(), object2, true, isOneWay);
        if (isExecuted()) {
            try {
                LinkManagerRemote linker = (LinkManagerRemote) RemoteLookupAgent.getManagerForClass(LinkManagerRemote.class);
                int[] vers = linker.link(getObject().getId(), object2.getId(), isOneWay, type);
                if (vers.length == 2) {
                    getObject().setVersion(vers[0]);
                    if (!isOneWay) {
                        object2.setVersion(vers[1]);
                    }
                }
                setUndone(true);
            } catch (MlLinkException | NonExistingObjectException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Object Undo Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public String toString() {
        return "Unlinking (" + getFormattedId() + " from " + getFormattedId(object2.getId()) + ")";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.object2 != null ? this.object2.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UnlinkCommand other = (UnlinkCommand) obj;
        if ((other.isOneWay == this.isOneWay && other.object2.equals(this.object2)) && other.getObject().equals(this.getObject())) {
            return true;
        } else {
        }
        return false;
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    public boolean isProceed() {
        return proceed;
    }

}
