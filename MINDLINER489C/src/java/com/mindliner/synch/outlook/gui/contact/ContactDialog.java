package com.mindliner.synch.outlook.gui.contact;

import com.mindliner.synch.outlook.gui.contact.event.ContactDialogEvent;
import com.mindliner.synch.outlook.gui.contact.event.ContactDialogListener;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import com.moyosoft.connector.com.*;
import com.moyosoft.connector.ms.outlook.contact.*;
import com.moyosoft.samples.outlook.gui.contact.handler.*;

public class ContactDialog extends JDialog implements IContactPanelHandler {

    private ContactPanel mContactPanel = new ContactPanel(this);
    private OutlookContact mContact = null;
    private ArrayList mListeners = new ArrayList();

    public ContactDialog(Frame pParent) {
        super(pParent);
        init();
    }

    protected void init() {
        setModal(true);

        setDefaultCloseOperation(
                javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(BorderLayout.CENTER, mContactPanel);
    }

    public void close() {
        this.dispose();

        mContactPanel.cleanUp();
        mContact = null;
    }

    public void load(OutlookContact pContact) throws ComponentObjectModelException {
        mContact = pContact;
        if (pContact != null) {
            mContactPanel.load(pContact);
        }
        contactChanged();
    }

    protected void contactChanged() throws ComponentObjectModelException {
        updateTitle();
        updateButtons();
    }

    private static String notNull(String pStr) {
        if (pStr == null) {
            return "";
        }
        return pStr;
    }

    protected void updateTitle() throws ComponentObjectModelException {
        if (mContact != null) {
            setTitle("Contact - " + notNull(mContact.getFirstName()) + " " + notNull(mContact.getLastName()));
        } else {
            setTitle("Contact");
        }
    }

    protected void updateButtons() {
        mContactPanel.setDeleteEnabled(mContact != null);
    }

    public boolean storeTo(OutlookContact pContact) throws ComponentObjectModelException {
        return mContactPanel.store(pContact);
    }

    public boolean hasChanged(OutlookContact pContact) throws ComponentObjectModelException {
        if (pContact != null) {
            return mContactPanel.hasChanged(pContact);
        }
        return true;
    }

    protected void fireSaveAndClosePressed() {
        ContactDialogEvent event = new ContactDialogEvent(this);
        event.setContact(mContact);
        for (int i = 0; i < mListeners.size(); i++) {
            ((ContactDialogListener) mListeners.get(i)).saveAndClosePressed(event);
        }
    }

    protected void fireDeletePressed() {
        ContactDialogEvent event = new ContactDialogEvent(this);
        event.setContact(mContact);
        for (int i = 0; i < mListeners.size(); i++) {
            ((ContactDialogListener) mListeners.get(i)).deletePressed(event);
        }
    }

    protected void fireClosePressed() {
        ContactDialogEvent event = new ContactDialogEvent(this);
        event.setContact(mContact);
        for (int i = 0; i < mListeners.size(); i++) {
            ((ContactDialogListener) mListeners.get(i)).closePressed(event);
        }
    }

    public void closePressed() {
        fireClosePressed();
    }

    public void saveAndClosePressed() {
        fireSaveAndClosePressed();
    }

    public void deletePressed() {
        fireDeletePressed();
    }

    public void addContactDialogListener(ContactDialogListener pListener) {
        mListeners.add(pListener);
    }

    public void removeContactDialogListener(ContactDialogListener pListener) {
        mListeners.remove(pListener);
    }

    private static void centerOnScreen(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = window.getSize();
        windowSize.height = Math.min(windowSize.height + 20, screenSize.height);
        windowSize.width = Math.min(windowSize.width, screenSize.width);
        window.setLocation((screenSize.width - windowSize.width) / 2,
                (screenSize.height - windowSize.height) / 2);
    }

    public static void open(Frame pParent, ContactDialogListener pListener, OutlookContact pContact) throws ComponentObjectModelException {
        ContactDialog dialog = new ContactDialog(pParent);
        dialog.addContactDialogListener(pListener);
        dialog.pack();
        centerOnScreen(dialog);

        dialog.load(pContact);

        dialog.show();
    }

    public static void open(Frame pParent, ContactDialogListener pListener) {
        try {
            open(pParent, pListener, null);
        } catch (ComponentObjectModelException e) {
        }
    }
}
