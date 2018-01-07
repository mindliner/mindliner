package com.mindliner.synch.outlook.gui;

import javax.swing.*;

public class Icons
{
   public static final Icon SAVE_ICON = loadIcon("imgs/save.gif");
   public static final Icon SAVEAS_ICON = loadIcon("imgs/saveas.gif");
   public static final Icon DELETE_ICON = loadIcon("imgs/delete.gif");
   public static final Icon DELETE_DISABLED_ICON  = loadIcon("imgs/delete_disabled.gif");
   public static final Icon ADD_ATTACHMENT_ICON = loadIcon("imgs/add_attachement.gif");
   public static final Icon ADD_FOLDER_ICON = loadIcon("imgs/add_folder.gif");
   public static final Icon REFRESH_ICON = loadIcon("imgs/refresh.gif");
   public static final Icon CUT_ICON = loadIcon("imgs/cut.gif");
   public static final Icon COPY_ICON = loadIcon("imgs/copy.gif");
   public static final Icon PASTE_ICON = loadIcon("imgs/paste.gif");

   public static final Icon FOLDER_CLOSED_ICON = loadIcon("imgs/folder_closed_icon.gif");
   public static final Icon FOLDER_OPEN_ICON = loadIcon("imgs/folder_open_icon.gif");

   private static Icon loadIcon(String pIconPath)
   {
     try
     {
       return new ImageIcon(
         Icons.class.getClassLoader().getResource(
            pIconPath));
     }
     catch(Exception ex)
     {
       return null;
     }
   }
}
