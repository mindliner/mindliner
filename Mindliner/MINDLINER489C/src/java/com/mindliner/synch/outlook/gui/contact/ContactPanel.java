package com.mindliner.synch.outlook.gui.contact;

import com.mindliner.synch.outlook.gui.Icons;
import javax.swing.*;
import java.awt.*;

import com.moyosoft.connector.com.*;
import com.moyosoft.connector.ms.outlook.contact.*;
import com.moyosoft.samples.outlook.gui.contact.handler.*;

import java.awt.event.*;

public class ContactPanel extends JPanel
{
   private JButton mButtonClose = new JButton("Close");
   private JButton mButtonSave = new JButton("Save and close", Icons.SAVE_ICON);
   private JButton mButtonDelete = new JButton(Icons.DELETE_ICON);
   private ContactAttributesPanel mContactAttributesPanel = new ContactAttributesPanel();

   private IContactPanelHandler mHandler = null;

   public ContactPanel(IContactPanelHandler pHandler)
   {
      super();
      mHandler = pHandler;
      init();
   }

   public ContactPanel(IContactPanelHandler pHandler, boolean pDoubleBuffered)
   {
      super(pDoubleBuffered);
      mHandler = pHandler;
      init();
   }

   protected void init()
   {
      createPanel();
   }

   protected void createPanel()
   {
      // boutons
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      buttonPanel.add(mButtonClose);

      // tool bar
      JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
      toolBar.add(mButtonSave);
     toolBar.add(mButtonDelete);
      mButtonSave.setFocusPainted(false);
     mButtonDelete.setFocusPainted(false);

      setLayout(new BorderLayout());
      add(BorderLayout.NORTH, toolBar);
      add(BorderLayout.CENTER, mContactAttributesPanel);
      add(BorderLayout.SOUTH, buttonPanel);

      // borders
      mContactAttributesPanel.setBorder(BorderFactory.createEmptyBorder(14, 7, 5, 7));
      buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));

      // listeners
      mButtonClose.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event)
         {
            closePressed();
         }
      });
      mButtonSave.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event)
         {
            saveAndClosePressed();
         }
      });
      mButtonDelete.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event)
         {
            deletePressed();
         }
      });
   }

	protected void cleanUp()
	{
		mHandler = null;
	}

   public void closePressed()
   {
      mHandler.closePressed();
   }

   public void saveAndClosePressed()
   {
      mHandler.saveAndClosePressed();
   }

   public void deletePressed()
   {
      mHandler.deletePressed();
   }

   public void load(OutlookContact pContact) throws ComponentObjectModelException
   {
      mContactAttributesPanel.load(pContact);
   }

   public boolean store(OutlookContact pContact) throws ComponentObjectModelException
   {
      return mContactAttributesPanel.store(pContact);
   }

   public boolean hasChanged(OutlookContact pContact) throws ComponentObjectModelException
   {
      return mContactAttributesPanel.hasChanged(pContact);
   }

   protected void setDeleteEnabled(boolean pEnabled)
   {
       mButtonDelete.setEnabled(pEnabled);
   }
}
