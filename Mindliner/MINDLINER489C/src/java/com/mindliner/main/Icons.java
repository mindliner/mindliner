
package com.mindliner.main;

import javax.swing.*;

public class Icons
{
   public static final Icon SAVE_ICON = loadIcon("img/save.gif");
   public static final Icon SAVEAS_ICON = loadIcon("img/saveas.gif");
   public static final Icon DELETE_ICON = loadIcon("img/delete.gif");
   public static final Icon DELETE_DISABLED_ICON  = loadIcon("img/delete_disabled.gif");
   public static final Icon ADD_ATTACHMENT_ICON = loadIcon("img/add_attachement.gif");
   public static final Icon ADD_FOLDER_ICON = loadIcon("img/add_folder.gif");
   public static final Icon REFRESH_ICON = loadIcon("img/refresh.gif");
   public static final Icon CUT_ICON = loadIcon("img/cut.gif");
   public static final Icon COPY_ICON = loadIcon("img/copy.gif");
   public static final Icon PASTE_ICON = loadIcon("img/paste.gif");
   public static final Icon FOLDER_CLOSED_ICON = loadIcon("img/folder_closed_icon.gif");
   public static final Icon FOLDER_OPEN_ICON = loadIcon("img/folder_open_icon.gif");
   public static final Icon TASK_COMPLETED = loadIcon("img/taskCompleted-20.jpg");
   
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
