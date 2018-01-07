package com.mindliner.synch.outlook.gui.contact;

import com.mindliner.synch.outlook.gui.attachment.AttachmentListPanel;
import javax.swing.*;
import java.awt.*;

import com.moyosoft.connector.com.*;
import com.moyosoft.connector.ms.outlook.contact.*;

public class ContactAttributesPanel extends JPanel
{
    // Labels
    private JLabel mLabelFirstname = new JLabel("First name");
    private JLabel mLabelLastname = new JLabel("Last name");
    private JLabel mLabelCompany = new JLabel("Company");
    private JLabel mLabelHomePhone = new JLabel("Home phone");
    private JLabel mLabelWorkPhone = new JLabel("Work phone");
    private JLabel mLabelMobilePhone = new JLabel("Mobile");
    private JLabel mLabelFaxNumber = new JLabel("Fax");
    private JLabel mLabelEmail = new JLabel("E-mail");
    private JLabel mLabelWebsite = new JLabel("Website");
    private JLabel mLabelAddress = new JLabel("Address");
    private JLabel mLabelBody = new JLabel("Body");
    private JLabel mLabelAttachements = new JLabel("Attachements");

    // Fields
    private JTextField mFieldFirstname = new JTextField();
    private JTextField mFieldLastname = new JTextField();
    private JTextField mFieldCompany = new JTextField();
    private JTextField mFieldHomePhone = new JTextField();
    private JTextField mFieldWorkPhone = new JTextField();
    private JTextField mFieldMobilePhone = new JTextField();
    private JTextField mFieldFaxNumber = new JTextField();
    private JTextField mFieldEmail = new JTextField();
    private JTextField mFieldWebsite = new JTextField();
    private JTextArea mFieldAddress = new JTextArea();
    private JTextArea mFieldBody = new JTextArea();
    private AttachmentListPanel mFieldAttahmentList = new AttachmentListPanel();

    public ContactAttributesPanel()
    {
        super();
        init();
    }

    public ContactAttributesPanel(boolean pDoubleBuffered)
    {
        super(pDoubleBuffered);
        init();
    }

    protected void init()
    {
        createPanel();

        setPreferredSize(new Dimension(600, 330));
    }

    protected void createPanel()
    {
        // Create panels
        JPanel panelInfoLeft = createInfoLeftPanel();
        JPanel panelInfoRight = createInfoRightPanel();
        JPanel panelInfoBottomLeft = createInfoBottomLeftPanel();
        JPanel panelInfoAddress = createInfoAddressPanel();
        JPanel panelBody = createBodyPanel();
        JPanel panelAttachements = createAttachementsPanel();

        // Etched separators
        EtchedLine separator1 = new EtchedLine();
        EtchedLine separator2 = new EtchedLine();
        EtchedLine separator3 = new EtchedLine();

        // GridBagLayout
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // insets between components
        Insets separatorInsets = new Insets(10, 3, 10, 3);
        Insets leftInsets = new Insets(0, 3, 0, 10);
        Insets rightInsets = new Insets(0, 10, 0, 3);

        // two columns: 50% of the width each
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;

        // 0,0
        c.gridx = 0;
        c.gridy = 0;
        c.insets = leftInsets;
        add(panelInfoLeft, c);

        // 1,0
        c.gridx = 1;
        c.gridy = 0;
        c.insets = rightInsets;
        add(panelInfoRight, c);

        // separator line: 0,1 and 1,1
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.insets = separatorInsets;
        add(separator1, c);
        c.gridwidth = 1;

        // 0,2
        c.gridx = 0;
        c.gridy = 2;
        c.insets = leftInsets;
        add(panelInfoBottomLeft, c);

        // 1,2
        c.gridx = 1;
        c.gridy = 2;
        c.insets = rightInsets;
        add(panelInfoAddress, c);

        // separator line: 0,3 and 1,3
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.insets = separatorInsets;
        add(separator2, c);
        c.gridwidth = 1;

        // 0,4 : fill all remaining height space
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 4;
        c.weighty = 1;
        c.insets = leftInsets;
        add(panelBody, c);

        // 1,4 : fill all remaining height space
        c.gridx = 1;
        c.gridy = 4;
        c.insets = rightInsets;
        add(panelAttachements, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;

        // separator line: 0,5 and 1,5
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        c.insets = new Insets(10, 3, 0, 3);
        add(separator3, c);
        c.gridwidth = 1;
    }

    private JPanel createInfoLeftPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.insets = new Insets(0, 0, 0, 7);
        labelConstraints.anchor = GridBagConstraints.WEST;

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1;

        // Last name
        // 0,0
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        panel.add(mLabelLastname, labelConstraints);

        // 1,0
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = 0;
        panel.add(mFieldLastname, fieldConstraints);

        // First name
        // 0,1
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 1;
        panel.add(mLabelFirstname, labelConstraints);

        // 1,1
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = 1;
        panel.add(mFieldFirstname, fieldConstraints);

        // Company
        // 0,2
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 2;
        panel.add(mLabelCompany, labelConstraints);

        // 1,2
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = 2;
        panel.add(mFieldCompany, fieldConstraints);

        return panel;
    }

    private JPanel createInfoRightPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.insets = new Insets(0, 0, 0, 7);
        labelConstraints.anchor = GridBagConstraints.WEST;

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1;

        // E-mail
        // 0,0
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        panel.add(mLabelEmail, labelConstraints);

        // 1,0
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = 0;
        panel.add(mFieldEmail, fieldConstraints);

        // Website
        // 0,1
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 1;
        panel.add(mLabelWebsite, labelConstraints);

        // 1,1
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = 1;
        panel.add(mFieldWebsite, fieldConstraints);

        return panel;
    }

    private JPanel createInfoBottomLeftPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.insets = new Insets(0, 0, 0, 7);
        labelConstraints.anchor = GridBagConstraints.WEST;

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1;

        // Home phone
        // 0,0
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        panel.add(mLabelHomePhone, labelConstraints);

        // 1,0
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = 0;
        panel.add(mFieldHomePhone, fieldConstraints);

        // Work phone
        // 0,1
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 1;
        panel.add(mLabelWorkPhone, labelConstraints);

        // 1,1
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = 1;
        panel.add(mFieldWorkPhone, fieldConstraints);

        // Mobile phone
        // 0,2
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 2;
        panel.add(mLabelMobilePhone, labelConstraints);

        // 1,2
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = 2;
        panel.add(mFieldMobilePhone, fieldConstraints);

        // Fax
        // 0,3
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 3;
        panel.add(mLabelFaxNumber, labelConstraints);

        // 1,3
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = 3;
        panel.add(mFieldFaxNumber, fieldConstraints);

        return panel;
    }

    private JPanel createInfoAddressPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(BorderLayout.NORTH, mLabelAddress);

        JScrollPane scrollPane = new JScrollPane(mFieldAddress);
        panel.add(BorderLayout.CENTER, scrollPane);

        panel.setPreferredSize(new Dimension(10, 90));
        return panel;
    }

    private JPanel createBodyPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(BorderLayout.NORTH, mLabelBody);

        JScrollPane scrollPane = new JScrollPane(mFieldBody);
        panel.add(BorderLayout.CENTER, scrollPane);

        panel.setPreferredSize(new Dimension(10, 10));

        return panel;
    }

    private JPanel createAttachementsPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(BorderLayout.NORTH, mLabelAttachements);
        panel.add(BorderLayout.CENTER, mFieldAttahmentList);

        panel.setPreferredSize(new Dimension(10, 10));

        return panel;
    }

    public void load(OutlookContact pContact)
            throws ComponentObjectModelException
    {
        if(pContact == null)
        {
            return;
        }

        mFieldAddress.setText(pContact.getHomeAddress());
        mFieldBody.setText(pContact.getBody());
        mFieldCompany.setText(pContact.getCompanyName());
        mFieldEmail.setText(pContact.getEmail1Address());
        mFieldFaxNumber.setText(pContact.getHomeFaxNumber());
        mFieldFirstname.setText(pContact.getFirstName());
        mFieldHomePhone.setText(pContact.getHomeTelephoneNumber());
        mFieldLastname.setText(pContact.getLastName());
        mFieldMobilePhone.setText(pContact.getMobileTelephoneNumber());
        mFieldWebsite.setText(pContact.getWebPage());
        mFieldWorkPhone.setText(pContact.getBusinessTelephoneNumber());

        // load attachments
        mFieldAttahmentList.load(pContact);
    }

    public boolean store(OutlookContact pContact)
            throws ComponentObjectModelException
    {
        boolean changed = hasChanged(pContact);
        if(changed)
        {
            storeAttributes(pContact);
        }

        return changed;
    }

    protected void storeAttributes(OutlookContact pContact)
            throws ComponentObjectModelException
    {
        pContact.setHomeAddress(mFieldAddress.getText());
        pContact.setBody(mFieldBody.getText());
        pContact.setCompanyName(mFieldCompany.getText());
        pContact.setEmail1Address(mFieldEmail.getText());
        pContact.setHomeFaxNumber(mFieldFaxNumber.getText());
        pContact.setFirstName(mFieldFirstname.getText());
        pContact.setHomeTelephoneNumber(mFieldHomePhone.getText());
        pContact.setLastName(mFieldLastname.getText());
        pContact.setMobileTelephoneNumber(mFieldMobilePhone.getText());
        pContact.setWebPage(mFieldWebsite.getText());
        pContact.setBusinessTelephoneNumber(mFieldWorkPhone.getText());
    }

    private static boolean equals(Object o1, Object o2)
    {
        if(o1 == o2)
            return true;
        if(o1 == null || o2 == null)
            return false;
        return o1.equals(o2);
    }

    protected boolean hasChanged(OutlookContact pContact)
            throws ComponentObjectModelException
    {
        boolean changed = false;
        changed |= !equals(pContact.getHomeAddress(), mFieldAddress.getText());
        changed |= !equals(pContact.getBody(), mFieldBody.getText());
        changed |= !equals(pContact.getCompanyName(), mFieldCompany.getText());
        changed |= !equals(pContact.getEmail1Address(), mFieldEmail.getText());
        changed |= !equals(pContact.getHomeFaxNumber(), mFieldFaxNumber
                .getText());
        changed |= !equals(pContact.getFirstName(), mFieldFirstname.getText());
        changed |= !equals(pContact.getHomeTelephoneNumber(), mFieldHomePhone
                .getText());
        changed |= !equals(pContact.getLastName(), mFieldLastname.getText());
        changed |= !equals(pContact.getMobileTelephoneNumber(),
                mFieldMobilePhone.getText());
        changed |= !equals(pContact.getWebPage(), mFieldWebsite.getText());
        changed |= !equals(pContact.getBusinessTelephoneNumber(),
                mFieldWorkPhone.getText());
        return changed;
    }
}
