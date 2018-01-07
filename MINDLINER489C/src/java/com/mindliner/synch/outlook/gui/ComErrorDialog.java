package com.mindliner.synch.outlook.gui;

import java.awt.*;

import com.moyosoft.connector.com.*;

public class ComErrorDialog {

    public static void open(Frame pParent, ComponentObjectModelException pEx) {
        MessageDialog.openOk(pParent, createMessage(pEx));
    }

    public static void open(Dialog pParent, ComponentObjectModelException pEx) {
        MessageDialog.openOk(pParent, createMessage(pEx));
    }

    public static void openDialog(Window pParent, ComponentObjectModelException pEx) {
        if (pParent instanceof Dialog) {
            open((Dialog) pParent, pEx);
        } else if (pParent instanceof Frame) {
            open((Frame) pParent, pEx);
        } else {
            MessageDialog.openOk(createMessage(pEx));
        }
    }

    private static String createMessage(ComponentObjectModelException pEx) {
        String message = pEx.getMessage();
        if (message == null || message.length() <= 0) {
            message = "Unknown COM error occured.";
        }

        return message;
    }
}
