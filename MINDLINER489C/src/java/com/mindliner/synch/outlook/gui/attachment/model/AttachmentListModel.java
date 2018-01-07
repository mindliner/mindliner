package com.mindliner.synch.outlook.gui.attachment.model;

import java.util.*;
import javax.swing.*;

import com.moyosoft.connector.com.*;
import com.moyosoft.connector.ms.outlook.attachment.*;

public class AttachmentListModel extends AbstractListModel {

    private List mAttachmentList = new ArrayList();

    public AttachmentListModel() {
    }

    public int getSize() {
        synchronized (mAttachmentList) {
            return mAttachmentList.size();
        }
    }

    public Object getElementAt(int row) {
        AttachmentItem attachment = getAttachmentItemAt(row);
        if (attachment != null) {
            return attachment.getName();
        }
        return "";
    }

    public OutlookAttachment getAttachmentAt(int row) {
        return getAttachmentItemAt(row).getAttachment();
    }

    protected AttachmentItem getAttachmentItemAt(int row) {
        synchronized (mAttachmentList) {
            return (AttachmentItem) mAttachmentList.get(row);
        }
    }

    public void clear() {
        synchronized (mAttachmentList) {
            int size = getSize();
            mAttachmentList.clear();
            fireIntervalRemoved(this, 0, size);
        }
    }

    public void add(OutlookAttachment pAttachment) throws ComponentObjectModelException {
        if (pAttachment == null) {
            return;
        }

        synchronized (mAttachmentList) {
            int size = getSize();
            mAttachmentList.add(new AttachmentItem(pAttachment));
            fireIntervalAdded(this, size, size);
        }
    }

    public void remove(int row) {
        synchronized (mAttachmentList) {
            mAttachmentList.remove(row);
            fireIntervalRemoved(this, row, row);
        }
    }

    class AttachmentItem {

        private String mName;
        private OutlookAttachment mItem;

        public AttachmentItem(OutlookAttachment pItem) throws ComponentObjectModelException {
            mItem = pItem;
            mName = pItem.getDisplayName();
        }

        public String getName() {
            return mName;
        }

        public OutlookAttachment getAttachment() {
            return mItem;
        }
    }
}
