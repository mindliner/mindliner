package com.mindliner.synch.outlook.folderchooser;

import java.util.*;

import javax.swing.tree.*;

import com.moyosoft.connector.com.*;
import com.moyosoft.connector.ms.outlook.folder.*;

public abstract class AbstractFolderTreeNode extends DefaultMutableTreeNode implements FolderTreeNode {

    private boolean childrensCreated = false;

    public AbstractFolderTreeNode(String name) {
        super(name);
    }

    protected void createChildrens(FoldersCollection folders) {
        if (childrensCreated) {
            return;
        }

        try {
            if (folders != null && folders.size() > 0) {
                List foldersList = new ArrayList();

                for (Iterator it = folders.iterator(); it.hasNext();) {
                    OutlookFolder folder = (OutlookFolder) it.next();
                    foldersList.add(folder);
                }

                Collections.sort(
                        foldersList,
                        OutlookFolderComparator.getInstance());

                for (Iterator it = foldersList.iterator(); it.hasNext();) {
                    OutlookFolder folder = (OutlookFolder) it.next();
                    FolderTreeNode node = new OutlookFolderTreeNode(folder);
                    add(node);
                }
            }

            childrensCreated = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refresh() throws ComponentObjectModelException {
        if (childrensCreated) {
            removeAllChildren();
            childrensCreated = false;
            createChildrens();
        }
    }

    public abstract OutlookFolder getFolder();

    public abstract void createChildrens();
}
