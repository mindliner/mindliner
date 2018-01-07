package com.mindliner.synch.outlook.gui.contact.event;

import com.mindliner.synch.outlook.gui.contact.ContactDialog;
import java.util.*;

import com.moyosoft.connector.ms.outlook.contact.*;

public class ContactDialogEvent extends EventObject {

    private OutlookContact mContact = null;

    public ContactDialogEvent(Object pSource) {
        super(pSource);
    }

    public ContactDialog getContactDialog() {
        return (ContactDialog) getSource();
    }

    public OutlookContact getContact() {
        return mContact;
    }

    public void setContact(OutlookContact pContact) {
        mContact = pContact;
    }
}
