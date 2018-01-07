package com.mindliner.synch.outlook.folderchooser;

import com.mindliner.main.Icons;
import java.awt.*;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.moyosoft.connector.com.*;
import com.moyosoft.connector.ms.outlook.folder.*;

public class FolderChooser extends JDialog implements TreeSelectionListener,
        TreeWillExpandListener {

    private OutlookFolder mSelectedFolder = null;
    private FoldersCollection mRootFolders = null;
    private JButton mButtonCancel = new JButton("Cancel");
    private JButton mButtonOk = new JButton("OK");
    private JTree mMainTree = new JTree();

    protected FolderChooser(FoldersCollection pFolders) {
        super();
        mRootFolders = pFolders;
        initialize();
    }

    protected FolderChooser(java.awt.Dialog dlg, FoldersCollection pFolders) {
        super(dlg);
        mRootFolders = pFolders;
        initialize();
    }

    protected FolderChooser(java.awt.Frame frame, FoldersCollection pFolders) {
        super(frame);
        mRootFolders = pFolders;
        initialize();
    }

    private void initialize() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(true);
        setSize(320, 320);
        setModal(true);
        setTitle("Choose folder");

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new java.awt.BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(mButtonOk);
        buttonsPanel.add(mButtonCancel);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(Icons.FOLDER_CLOSED_ICON);
        renderer.setClosedIcon(Icons.FOLDER_CLOSED_ICON);
        renderer.setOpenIcon(Icons.FOLDER_OPEN_ICON);

        mMainTree.setShowsRootHandles(true);
        mMainTree.setRootVisible(false);
        mMainTree.setCellRenderer(renderer);
        mMainTree.addMouseListener(new FolderPopupMenu(this));

        JScrollPane scrollPanel = new JScrollPane();
        scrollPanel.setViewportView(mMainTree);

        contentPanel.add(scrollPanel, BorderLayout.CENTER);
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);

        java.awt.Dimension dim = getToolkit().getScreenSize();
        java.awt.Rectangle abounds = getBounds();
        setLocation((dim.width - abounds.width) / 2,
                (dim.height - abounds.height) / 2);
        requestFocus();

        try {
            FolderTreeNode rootNode = new OutlookFolderRootNode(mRootFolders);
            rootNode.createChildrens();

            mMainTree.setModel(new DefaultTreeModel(rootNode));
            mMainTree.expandPath(new TreePath(new Object[]{rootNode}));
        } catch (ComponentObjectModelException e) {
            e.printStackTrace();
        }

        mMainTree.addTreeWillExpandListener(this);
        mMainTree.addTreeSelectionListener(this);

        mButtonOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pArg0) {
                okPressed();
            }
        });

        mButtonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pArg0) {
                cancelPressed();
            }
        });
    }

    protected JTree getTree() {
        return mMainTree;
    }

    public OutlookFolder getChoosedFolder() {
        return mSelectedFolder;
    }

    protected OutlookFolder getSelectedFolder() {
        return getFolderForPath(mMainTree.getSelectionPath());
    }

    protected OutlookFolder getFolderForPath(TreePath pPath) {
        if (pPath == null) {
            return null;
        }

        return ((FolderTreeNode) (pPath.getLastPathComponent())).getFolder();
    }

    protected void expandSelectedItem() {
        TreePath path = mMainTree.getSelectionPath();

        if (path != null) {
            mMainTree.expandPath(path);
        }
    }

    protected void collapseSelectedItem() {
        TreePath path = mMainTree.getSelectionPath();

        if (path != null) {
            mMainTree.collapsePath(path);
        }
    }

    protected void refreshNode(FolderTreeNode pNode) {
        if (pNode != null) {
            try {
                pNode.refresh();
                ((DefaultTreeModel) mMainTree.getModel()).reload(pNode);
            } catch (ComponentObjectModelException e) {
                e.printStackTrace();
            }
        }
    }

    protected void refreshSelectedItem() {
        TreePath path = mMainTree.getSelectionPath();

        if (path != null) {
            refreshNode((FolderTreeNode) path.getLastPathComponent());
        }
    }

    protected void refreshAll() {
        FolderTreeNode rootNode = (FolderTreeNode) mMainTree.getModel()
                .getRoot();

        if (rootNode != null) {
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                refreshNode((FolderTreeNode) rootNode.getChildAt(i));
            }
        }
    }

    protected TreePath getSelectedPath() {
        return mMainTree.getSelectionPath();
    }

    protected void removeSelectionPath(TreePath pPath) {
        if (pPath != null) {
            FolderTreeNode node = (FolderTreeNode) pPath.getLastPathComponent();

            ((DefaultTreeModel) mMainTree.getModel())
                    .removeNodeFromParent(node);

            refreshAll();
        }
    }

    public void cancelPressed() {
        mSelectedFolder = null;
        dispose();
    }

    public void okPressed() {
        mSelectedFolder = getSelectedFolder();
        dispose();
    }

    public void treeWillCollapse(TreeExpansionEvent event)
            throws ExpandVetoException {
    }

    public void treeWillExpand(TreeExpansionEvent event)
            throws ExpandVetoException {
        if (event.getSource() == mMainTree) {
            Object o = event.getPath().getLastPathComponent();

            if (o instanceof FolderTreeNode) {
                ((FolderTreeNode) o).createChildrens();
            }
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
    }

    public static OutlookFolder open(Frame pParentFrame,
            FoldersCollection pFolders) {
        final FolderChooser dlg = new FolderChooser(pParentFrame, pFolders);

        dlg.setModal(true);
        dlg.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                dlg.dispose();
            }
        ;
        });
        dlg.show();

        return dlg.getChoosedFolder();
    }

    public static OutlookFolder open(Dialog pParentDialog,
            FoldersCollection pFolders) {
        final FolderChooser dlg = new FolderChooser(pParentDialog, pFolders);

        dlg.setModal(true);
        dlg.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                dlg.dispose();
            }
        ;
        });
        dlg.show();

        return dlg.getChoosedFolder();
    }

    public static OutlookFolder open(FoldersCollection pFolders) {
        final FolderChooser dlg = new FolderChooser(pFolders);

        dlg.setModal(true);
        dlg.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                dlg.dispose();
            }
        ;
        });
        dlg.show();

        return dlg.getChoosedFolder();
    }
}
