package com.mindliner.synch.outlook.gui.model;

import java.util.*;

import javax.swing.table.*;

import com.moyosoft.connector.ms.outlook.contact.*;
import com.moyosoft.connector.ms.outlook.folder.*;
import com.moyosoft.connector.ms.outlook.item.*;
import com.moyosoft.connector.com.*;

public class ContactsTableModel extends AbstractTableModel
{
	private ArrayList mContacts = new ArrayList();
	private String[] mHeaders =
		new String[] { "First name", "Last name", "Home phone", "E-mail" };

	public ContactsTableModel()
	{
	}

	public synchronized void loadContacts(OutlookFolder pFolder)
		throws ComponentObjectModelException
	{
		mContacts.clear();
		fireTableDataChanged();

		if (pFolder == null)
		{
			return;
		}

		int row = 0;
		ItemsCollection items = pFolder.getItems();
		if (items != null)
		{
			ItemsIterator it = items.iterator();
			
			while(it.hasNext())
			{
				OutlookItem item = it.nextItem();
				if (item.getType().isContact())
				{
					mContacts.add(new ListContact((OutlookContact) item));
					fireTableRowsInserted(row, row);
					row++;
				}
			}
		}

		fireTableDataChanged();
	}

	public synchronized void addNewContact(OutlookContact pContact)
		throws ComponentObjectModelException
	{
		mContacts.add(new ListContact(pContact));
		fireTableDataChanged();
	}

	public synchronized void updateContact(OutlookContact pContact)
		throws ComponentObjectModelException
	{
		ListContact contact = getListContactById(pContact.getItemId());
		if (contact != null)
		{
			int index = mContacts.indexOf(contact);
			if (index >= 0)
			{
				mContacts.set(index, new ListContact(pContact));
				fireTableDataChanged();
			}
		}
	}

	protected synchronized ListContact getListContactById(OutlookItemID pId)
	{
		if (pId != null)
		{
			for (int i = 0; i < mContacts.size(); i++)
			{
				ListContact contact = (ListContact) mContacts.get(i);
				OutlookItemID id = contact.getId();
				if (pId.equals(id))
				{
					return contact;
				}
			}
		}
		return null;
	}

	public synchronized void removeContactById(OutlookItemID pId)
	{
		mContacts.remove(getListContactById(pId));
		fireTableDataChanged();
	}

	public String getColumnName(int column)
	{
		return mHeaders[column];
	}

	public int getRowCount()
	{
		return mContacts.size();
	}

	public int getColumnCount()
	{
		return mHeaders.length;
	}

	public Object getValueAt(int row, int column)
	{
		ListContact contact = (ListContact) mContacts.get(row);
		if (contact != null)
		{
			switch (column)
			{
				case 0 :
					return contact.getFirstname();
				case 1 :
					return contact.getLastname();
				case 2 :
					return contact.getPhone();
				case 3 :
					return contact.getEmail();
			}
		}
		return "";
	}

	public ListContact getContactAtRow(int row)
	{
		return (ListContact) mContacts.get(row);
	}

	public OutlookItemID getContactItemIdAtRow(int row)
	{
		ListContact contact = getContactAtRow(row);
		if (contact != null)
		{
			return contact.getId();
		}
		return null;
	}
}
