package com.mindliner.synch.outlook.folderchooser;

import com.moyosoft.connector.ms.outlook.folder.*;

class OutlookFolderRootNode extends AbstractFolderTreeNode
{
    private FoldersCollection mFolders;

    public OutlookFolderRootNode(FoldersCollection folders)
    {
        super("");
        mFolders = folders;
    }

    public void createChildrens()
    {
        createChildrens(mFolders);
    }

    public boolean isLeaf()
    {
        return false;
    }

    public OutlookFolder getFolder()
    {
        return null;
    }
}
