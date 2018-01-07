package com.mindliner.synch.outlook.gui.attachment;

import com.mindliner.synch.outlook.gui.ComErrorDialog;
import com.mindliner.synch.outlook.gui.Icons;
import com.mindliner.synch.outlook.gui.attachment.model.AttachmentListModel;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.moyosoft.connector.com.*;
import com.moyosoft.connector.ms.outlook.attachment.*;

import java.io.File;

public class AttachmentListPanel extends JPanel
{
    private JButton mButtonAdd = new JButton(Icons.ADD_ATTACHMENT_ICON);
    private JButton mButtonRemove = new JButton(Icons.DELETE_ICON);
    private JButton mButtonSaveAs = new JButton(Icons.SAVEAS_ICON);
    private JList mAttachmentList = new JList();
    private AttachmentListModel mListModel = new AttachmentListModel();
    private IAttachmentsContainer mContainer = null;

    public AttachmentListPanel()
    {
        super();
        init();
    }

    public AttachmentListPanel(boolean pDoubleBuffered)
    {
        super(pDoubleBuffered);
        init();
    }

    protected void init()
    {
        createPanel();
    }

    protected void createPanel()
    {
        // set the model
        mAttachmentList.setModel(mListModel);

        // icons
        mButtonRemove.setDisabledIcon(Icons.DELETE_DISABLED_ICON);
        mButtonRemove.setEnabled(false);

        mButtonSaveAs.setEnabled(false);

        // boutons
        JToolBar buttonPanel = new JToolBar(JToolBar.VERTICAL);
        buttonPanel.setBorderPainted(false);
        buttonPanel.setBorder(null);
        buttonPanel.add(mButtonAdd);
        buttonPanel.add(mButtonRemove);
        buttonPanel.add(mButtonSaveAs);

        JScrollPane scrollPane = new JScrollPane(mAttachmentList);

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, scrollPane);
        add(BorderLayout.EAST, buttonPanel);

        // listeners
        mButtonAdd.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                addPressed();
            }
        });
        mButtonRemove.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                removePressed();
            }
        });
        mButtonSaveAs.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                saveAsPressed();
            }
        });
        mAttachmentList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent listSelectionEvent)
            {
                listSelectionChanged();
            }
        });
    }

    protected void addPressed()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(getTopLevelAncestor());

        File f = chooser.getSelectedFile();

        if(f != null)
        {
            try
            {
                OutlookAttachment attachment = mContainer.getAttachments().add(
                        f);
                if(attachment != null)
                {
                    mListModel.add(attachment);
                }
            }
            catch(ComponentObjectModelException ex)
            {
                ComErrorDialog.openDialog(getParentWindow(), ex);
            }
        }
    }

    protected void removePressed()
    {
        int row = mAttachmentList.getSelectedIndex();

        OutlookAttachment attachment = mListModel.getAttachmentAt(row);
        try
        {
            attachment.delete();
            mListModel.remove(row);
        }
        catch(ComponentObjectModelException ex)
        {
            ComErrorDialog.openDialog(getParentWindow(), ex);
        }
    }

    protected void saveAsPressed()
    {
        int row = mAttachmentList.getSelectedIndex();

        OutlookAttachment attachment = mListModel.getAttachmentAt(row);
        if(attachment != null)
        {
            try
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(attachment.getFileName()));
                chooser.showSaveDialog(getTopLevelAncestor());

                File f = chooser.getSelectedFile();

                if(f != null)
                {
                    attachment.saveAsFile(f);
                }
            }
            catch(ComponentObjectModelException ex)
            {
                ComErrorDialog.openDialog(getParentWindow(), ex);
            }
        }
    }

    protected void listSelectionChanged()
    {
        int row = mAttachmentList.getSelectedIndex();
        mButtonRemove.setEnabled(row >= 0);
        mButtonSaveAs.setEnabled(row >= 0);
    }

    protected Window getParentWindow()
    {
        Container c = this.getTopLevelAncestor();
        if(c instanceof Window)
        {
            return (Window) c;
        }
        return null;
    }

    protected void load(AttachmentsCollection pAttachments)
            throws ComponentObjectModelException
    {
        mListModel.clear();

        if(pAttachments == null)
        {
            return;
        }

        for(AttachmentsIterator it = pAttachments.iterator(); it.hasNext();)
        {
            mListModel.add(it.nextItem());
        }
    }

    public void load(IAttachmentsContainer pContainer)
            throws ComponentObjectModelException
    {
        mContainer = pContainer;
        if(pContainer != null)
        {
            load(pContainer.getAttachments());
        }
    }
}
