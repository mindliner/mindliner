/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.MindlinerMain;
import com.mindliner.managers.LinkManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.Date;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * Updates the label of a connection/link.
 *
 * @author Marius Messerli
 */
public class LinkLabelUpdateCommand extends MindlinerOnlineCommand {

    private MlcLink link;
    private String label;
    private String previousLabel = "";

    public LinkLabelUpdateCommand(mlcObject o, MlcLink link, String label) {
        super(o, true);
        if (link == null) {
            throw new IllegalArgumentException("Link must not be null;");
        }
        this.link = link;
        previousLabel = link.getLabel();
        this.label = label;
        link.setLabel(label);
        link.setModificationDate(new Date());
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        LinkManagerRemote linker = (LinkManagerRemote) RemoteLookupAgent.getManagerForClass(LinkManagerRemote.class);
        try {
            linker.updateLinkLabel(link.getId(), label);
        } catch (NonExistingObjectException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex, "Label Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        link.setLabel(previousLabel);
        if (isExecuted()) {
            LinkManagerRemote linker = (LinkManagerRemote) RemoteLookupAgent.getManagerForClass(LinkManagerRemote.class);
            try {
                linker.updateLinkLabel(link.getId(), previousLabel);
            } catch (NonExistingObjectException ex) {
                JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex, "Label Undo Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public boolean isVersionChecking() {
        return true;
    }

}
