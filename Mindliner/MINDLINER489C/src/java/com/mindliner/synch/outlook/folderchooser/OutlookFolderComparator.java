package com.mindliner.synch.outlook.folderchooser;

import java.util.Comparator;
import com.moyosoft.connector.ms.outlook.folder.OutlookFolder;

class OutlookFolderComparator implements Comparator
{
    private static OutlookFolderComparator mInstance = null;

    private OutlookFolderComparator()
    {
    }

    public static OutlookFolderComparator getInstance()
    {
        if(mInstance == null)
        {
            mInstance = new OutlookFolderComparator();
        }
        return mInstance;
    }

    @Override
    public int compare(Object o1, Object o2)
    {
        OutlookFolder child1 = (OutlookFolder) o1;
        OutlookFolder child2 = (OutlookFolder) o2;

        if(child1.getName() == null)
        {
            return -1;
        }
        if(child2.getName() == null)
        {
            return 1;
        }
        return child1.getName().compareToIgnoreCase(child2.getName());
    }
}
