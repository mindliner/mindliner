package com.mindliner.synch.outlook.folderchooser;

import javax.swing.tree.*;

import com.moyosoft.connector.ms.outlook.folder.*;

public interface FolderTreeNode extends MutableTreeNode
{
	public void refresh();
    public OutlookFolder getFolder();
    public void createChildrens();
}
