package com.mindliner.synch.outlook.gui.model;

import com.moyosoft.connector.com.*;
import com.moyosoft.connector.ms.outlook.contact.*;
import com.moyosoft.connector.ms.outlook.item.*;

public class ListContact
{
	private String mFirstname = null;
	private String mLastname = null;
	private String mPhone = null;
	private String mEmail = null;
	private OutlookItemID mId = null;
	
	public ListContact(OutlookContact pContact) throws ComponentObjectModelException
	{
		mFirstname = pContact.getFirstName();
		mLastname = pContact.getLastName();
		mPhone = pContact.getHomeTelephoneNumber();
		mEmail = pContact.getEmail1Address();
		mId = pContact.getItemId();
	}
	
	public ListContact()
	{
	}
	
	public String getEmail()
	{
		return mEmail;
	}

	public String getFirstname()
	{
		return mFirstname;
	}

	public OutlookItemID getId()
	{
		return mId;
	}

	public String getLastname()
	{
		return mLastname;
	}

	public String getPhone()
	{
		return mPhone;
	}

	public void setEmail(String string)
	{
		mEmail = string;
	}

	public void setFirstname(String string)
	{
		mFirstname = string;
	}

	public void setId(OutlookItemID itemID)
	{
		mId = itemID;
	}

	public void setLastname(String string)
	{
		mLastname = string;
	}

	public void setPhone(String string)
	{
		mPhone = string;
	}
}
