/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.Colorizer;
import com.mindliner.entities.MlsColor;
import com.mindliner.entities.MlsColorScheme;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.InsufficientAccessRightException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.objects.transfer.MltColor;
import com.mindliner.objects.transfer.MlTransferColorScheme;
import com.mindliner.objects.transfer.MltColorizer;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Implements a color scheme used in Mindliner.
 *
 * @author Marius Messerli
 */
@Stateless
@DeclareRoles(value = {"MasterAdmin", "Admin", "User"})
@RolesAllowed(value = {"MasterAdmin", "Admin", "User"})
public class ColorManagerBean implements ColorManagerRemote {

    @PersistenceContext
    private EntityManager em;
    @EJB
    UserManagerLocal userManager;

    @Override
    public List<MlTransferColorScheme> getAccessibleSchemes() {
        Query q = em.createNamedQuery("MlsColorScheme.getAccessibleSchemes");
        q.setParameter("userId", userManager.getCurrentUser().getId());
        List<MlsColorScheme> resultList = q.getResultList();
        List<MlTransferColorScheme> schemes = new ArrayList<>();
        for (MlsColorScheme s : resultList) {
            MlTransferColorScheme st = new MlTransferColorScheme(s);
            schemes.add(st);
        }
        return schemes;
    }

    @Override
    public MlTransferColorScheme getScheme(int schemeId) throws NonExistingObjectException, InsufficientAccessRightException {
        for (MlTransferColorScheme tcs : getAccessibleSchemes()) {
            if (tcs.getId() == schemeId) {
                return tcs;
            }
        }
        throw new NonExistingObjectException("No scheme with the specified Id is accessible");
    }

    @Override
    public int createNewCustomScheme(MlTransferColorScheme scheme) throws NonExistingObjectException {
        mlsUser u = userManager.getCurrentUser();
        MlsColorScheme newScheme = new MlsColorScheme();
        newScheme.setName(scheme.getName());
        newScheme.setOwner(u);
        List<Colorizer> colorizers = new ArrayList<>();
        for (MltColorizer tColorizer : scheme.getColorizers()) {
            Colorizer sColorizer = new Colorizer();
            sColorizer.setColorizerClassName(tColorizer.getColorizerClassName());
            sColorizer.setMaximum(tColorizer.getMaximum());
            sColorizer.setMinimumOrThreshold(tColorizer.getMinimumOrThreshold());
            sColorizer.setScheme(newScheme);
            sColorizer.setType(tColorizer.getType());
            List<MlsColor> colors = new ArrayList<>();
            for (MltColor tColor : tColorizer.getColors()) {
                MlsColor sColor = new MlsColor();
                sColor.setColor(tColor.getColor());
                sColor.setColorizer(sColorizer);
                sColor.setDriverValue(tColor.getDriverValue());
                sColor.setId(tColor.getId());
                colors.add(sColor);
            }
            sColorizer.setColors(colors);
            colorizers.add(sColorizer);
        }
        newScheme.setColorizers(colorizers);
        em.persist(newScheme);
        em.flush();
        return newScheme.getId();
    }

    @Override
    public void deleteCustomScheme(int schemeId) throws InsufficientAccessRightException, NonExistingObjectException {
        mlsUser u = userManager.getCurrentUser();
        Query nq = em.createNamedQuery("mlsUser.getUsersWithSharedDataPools");
        nq.setParameter("userId", u.getId());
        List poolMates = nq.getResultList();
        MlsColorScheme s = em.find(MlsColorScheme.class, schemeId);
        if (s != null) {
            if (poolMates.contains(s.getOwner())) {
                em.remove(s);
            } else {
                throw new ForeignOwnerException("The specified object belongs to a user outside your data pools");
            }
        } else {
            throw new NonExistingObjectException("No such object");
        }
    }
}
