package com.mindliner.synch.outlook.gui.contact.event;

import java.util.*;

public interface ContactDialogListener extends EventListener
{
   public void closePressed(ContactDialogEvent pEvent);
   public void saveAndClosePressed(ContactDialogEvent pEvent);
   public void deletePressed(ContactDialogEvent pEvent);
}
