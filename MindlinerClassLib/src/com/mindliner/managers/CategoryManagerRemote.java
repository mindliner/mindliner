/*
 * TaskManagerRemote.java
 *
 * Created on 09.08.2007, 12:12:01
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.*;
import java.util.List;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;

/**
 *
 * @author Marius Messerli
 */
@Remote
@DeclareRoles(value = {"MasterAdmin", "Admin", "User"})
public interface CategoryManagerRemote {

    // For all Category Objects
    public mlsMindlinerCategory update(mlsMindlinerCategory mc);

    public mlsMindlinerCategory storeNew(mlsMindlinerCategory mc);

    public void remove(mlsMindlinerCategory mc);

    public mlsMindlinerCategory find(Class c, int key);

    // Priority Methods
    @RolesAllowed(value = {"Admin", "MasterAdmin"})
    public mlsPriority storeNewPriority(mlsPriority p);

    @RolesAllowed(value = {"Admin", "MasterAdmin"})
    public void removePriority(Object priority);

    public mlsPriority findPriority(int key);

    public List<mlsPriority> getAllPriorities();

    // Action Item Type
    public void storeNewActionItemType(MlsNewsType type);

    public MlsNewsType updateActionItemType(MlsNewsType type);

    public MlsNewsType findActionItemType(int key);

    public MlsNewsType findActionItemType(String tag);

    public List<MlsNewsType> getAllActionItemTypes();
}
