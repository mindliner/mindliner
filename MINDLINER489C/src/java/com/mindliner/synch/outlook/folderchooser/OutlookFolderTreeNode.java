package com.mindliner.synch.outlook.folderchooser;

import com.moyosoft.connector.ms.outlook.folder.*;

class OutlookFolderTreeNode extends AbstractFolderTreeNode {

    private OutlookFolder mFolder;

    public OutlookFolderTreeNode(OutlookFolder folder) {
        super(folder.getName());
        mFolder = folder;
    }

    public void createChildrens() {
        if (mFolder != null && mFolder.hasChildren()) {
            createChildrens(mFolder.getFolders());
        }
    }

    public OutlookFolder getFolder() {
        return mFolder;
    }

    public boolean isLeaf() {
        if (mFolder != null) {
            return !mFolder.hasChildren();
        }
        return false;
    }
}
