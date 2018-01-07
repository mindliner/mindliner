package com.mindliner.synch.outlook.folderchooser;

import com.mindliner.main.Icons;
import com.mindliner.synch.outlook.gui.ComErrorDialog;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

import com.moyosoft.connector.com.*;
import com.moyosoft.connector.ms.outlook.folder.*;

class FolderPopupMenu extends JPopupMenu implements MouseListener
{
    private FolderChooser mParent = null;
    private boolean mReadOnly = false;

    private JMenuItem mItemNewFolder = new JMenuItem("New folder...", Icons.ADD_FOLDER_ICON);
    private JMenuItem mItemDelete = new JMenuItem("Delete", Icons.DELETE_ICON);
    private JMenuItem mItemRefresh = new JMenuItem("Refresh", Icons.REFRESH_ICON);
    private JMenuItem mItemExpand = new JMenuItem("Expand");
    private JMenuItem mItemCollapse = new JMenuItem("Collapse");

    public FolderPopupMenu(FolderChooser pParent)
    {
        mParent = pParent;
        init();
    }

    protected void init()
    {
        add(mItemNewFolder);
        add(mItemDelete);
        add(new JSeparator());
        add(mItemRefresh);
        add(new JSeparator());
        add(mItemExpand);
        add(mItemCollapse);

        Dimension prefSize = getPreferredSize();
        if(prefSize != null)
        {
            setPreferredSize(new Dimension(Math.max(140, prefSize.width),
                    prefSize.height));
        }

        mItemNewFolder.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createNewFolder();
            }
        });

        mItemDelete.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                deleteSelectedFolder();
            }
        });

        mItemExpand.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                expandSelectedFolder();
            }
        });

        mItemCollapse.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                collapseSelectedFolder();
            }
        });

        mItemRefresh.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                refreshSelectedFolder();
            }
        });
    }

    protected void createNewFolder()
    {
        if(isReadOnly())
        {
            return;
        }

        NewFolderDialog dialog = new NewFolderDialog(mParent, "New folder");
        dialog.setModal(true);
        dialog.show();

        if(dialog.isOkPressed())
        {
            String folderName = dialog.getFolderName();
            FolderType folderType = dialog.getFolderType();

            if(folderName != null && folderType != null
                    && folderName.length() > 0)
            {
                try
                {
                    TreePath path = mParent.getSelectedPath();

                    if(path != null)
                    {
                        OutlookFolder folder = mParent.getFolderForPath(path);

                        if(folder != null)
                        {
                            folder.createFolder(folderName, folderType);
                            mParent.refreshNode((OutlookFolderTreeNode) path
                                    .getLastPathComponent());
                        }
                    }
                }
                catch(ComponentObjectModelException e)
                {
                    ComErrorDialog.open(mParent, e);
                }
            }
        }
    }

    protected void deleteSelectedFolder()
    {
        if(isReadOnly())
        {
            return;
        }

        try
        {
            TreePath path = mParent.getSelectedPath();

            if(path != null)
            {
                OutlookFolder folder = mParent.getFolderForPath(path);

                if(folder != null)
                {
                    folder.delete();
                    mParent.removeSelectionPath(path);
                }
            }
        }
        catch(ComponentObjectModelException e)
        {
            ComErrorDialog.open(mParent, e);
        }
    }

    protected void expandSelectedFolder()
    {
        mParent.expandSelectedItem();
    }

    protected void collapseSelectedFolder()
    {
        mParent.collapseSelectedItem();
    }

    protected void refreshSelectedFolder()
    {
        mParent.refreshSelectedItem();
    }

    protected void setReadOnly(boolean pReadOnly)
    {
        mReadOnly = pReadOnly;
    }

    protected boolean isReadOnly()
    {
        return mReadOnly;
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mousePressed(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    private void updateMenu()
    {
        mItemRefresh.setEnabled(true);

        if(isReadOnly())
        {
            mItemDelete.setEnabled(false);
            mItemNewFolder.setEnabled(false);
        }
        else
        {
            mItemDelete.setEnabled(true);
            mItemNewFolder.setEnabled(true);
        }

        OutlookFolder folder = mParent.getSelectedFolder();
        if(folder != null && folder.hasChildren())
        {
            mItemExpand.setEnabled(true);
            mItemCollapse.setEnabled(true);
        }
        else
        {
            mItemExpand.setEnabled(false);
            mItemCollapse.setEnabled(false);
        }
    }

    private void maybeShowPopup(MouseEvent e)
    {
        if(e.isPopupTrigger())
        {
            int row = mParent.getTree().getRowForLocation(e.getX(), e.getY());

            if(row >= 0)
            {
                mParent.getTree().setSelectionRow(row);

                updateMenu();
                show(e.getComponent(), e.getX() + 2, e.getY() - 1);
            }
        }
    }
}
