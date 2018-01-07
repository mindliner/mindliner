/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.entities.MlsImage;
import com.mindliner.entities.MlsImage.ImageType;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.InsufficientAccessRightException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.img.icons.MlIconLoader;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.swing.ImageIcon;

/**
 * This bean manages icon and profile images.
 *
 * @author Marius Messerli
 */
@Stateless
@DeclareRoles(value = {"Admin", "User", "MasterAdmin"})
public class ImageManager implements ImageManagerRemote, ImageManagerLocal {

    @EJB
    UserManagerLocal userManager;
    @PersistenceContext
    EntityManager em;
    @EJB
    ObjectFactoryLocal objectFactory;
    @EJB
    ObjectManagerLocal objectManager;
    @EJB
    LinkManagerRemote linkManager;
    @EJB
    SearchManagerRemote searchManager;
    @Resource
    EJBContext ctx;
    @EJB
    SolrServerBean solrServer;

    @Override
    @RolesAllowed(value = {"User", "Admin", "MasterAdmin"})
    public int updateImage(int imageId, ImageIcon image, MlsImage.ImageType type, String headline, String URL) throws NonExistingObjectException {
        MlsImage img = em.find(MlsImage.class, imageId);
        if (img == null) {
            throw new NonExistingObjectException("No image with that id was found");
        }
        img.setImage(image);
        if (image != null) {
            img.setPixelSizeX(image.getIconWidth());
            img.setPixelSizeY(image.getIconHeight());
            if (image.getDescription() != null && !image.getDescription().isEmpty()) {
                img.setDescription(image.getDescription());
            }
        }
        img.setType(type);
        img.setHeadline(headline);
        img.setUrl(URL);
        img.setModificationDate(new Date());
        em.flush();
        MlMessageHandler mh = new MlMessageHandler();
        mh.sendMessage(userManager.getCurrentUser(), img, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "image");
        mh.closeConnection();

        solrServer.addObject(img, true);
        return img.getVersion();
    }

    @Override
    @RolesAllowed(value = {"User", "Admin", "MasterAdmin"})
    public void addIconImages(int clientId, List<ImageIcon> icons) {
        mlsClient dataPool = em.find(mlsClient.class, clientId);
        addIconImages(dataPool, icons);
    }

    @Override
    @RolesAllowed(value = {"User", "Admin", "MasterAdmin"})
    public void addIconImages(mlsClient dataPool, List<ImageIcon> icons) {
        if (dataPool != null) {
            mlsUser u = userManager.getCurrentUser();
            for (ImageIcon icon : icons) {
                MlsImage img = new MlsImage();
                img.setClient(dataPool);
                Query nq = em.createNamedQuery("mlsConfidentiality.getMostPublicForClient");
                nq.setParameter("clientId", dataPool.getId());
                mlsConfidentiality mostPublicConf = (mlsConfidentiality) nq.getSingleResult();
                img.setConfidentiality(mostPublicConf);
                img.setOwner(u);
                img.setPrivateAccess(false);
                img.setPixelSizeX(icon.getIconWidth());
                img.setPixelSizeY(icon.getIconHeight());
                img.setImage(icon);
                img.setType(MlsImage.ImageType.Icon);
                if (icon.getDescription() != null) {
                    img.setHeadline(icon.getDescription());
                }
                solrServer.addObject(img, false);
                em.persist(img);
            }
            solrServer.commit();
        }
    }

    @Override
    @RolesAllowed(value = {"Admin", "MasterAdmin"})
    public void deleteAllIcons(int clientId) {
        mlsClient client = em.find(mlsClient.class, clientId);
        deleteAllIcons(client);
    }

    @Override
    @RolesAllowed(value = {"Admin", "MasterAdmin"})
    public void deleteAllIcons(mlsClient client) {
        if (client != null) {
            Query q = em.createNamedQuery("MlsImage.getIconsForDataPool");
            q.setParameter("imageType", ImageType.Icon);
            q.setParameter("clientId", client.getId());
            List<MlsImage> icons = q.getResultList();
            for (MlsImage icon : icons) {
                solrServer.removeObject(icon, false);
                em.remove(icon);
            }
            solrServer.commit();
        }
    }

    @Override
    @RolesAllowed(value = {"MasterAdmin", "Admin", "User"})
    public List<Integer> getAllAccessibleIconIds() {
        Query q = em.createNamedQuery("MlsImage.getIconsForUser");
        q.setParameter("imageType", ImageType.Icon);
        q.setParameter("user", userManager.getCurrentUser());
        return mlsObject.getIds(q.getResultList());
    }

    @Override
    @RolesAllowed(value = {"Admin", "MasterAdmin"})
    public List<Integer> getIconIds(int clientId) throws NonExistingObjectException {
        mlsClient c = em.find(mlsClient.class, clientId);
        if (c == null) {
            return new ArrayList<>();
        }
        return mlsObject.getIds((List) getIcons(c));
    }

    @Override
    @RolesAllowed(value = {"User", "Admin", "MasterAdmin"})
    public List<MlsImage> getIcons(mlsClient client) {
        Query q = em.createNamedQuery("MlsImage.getIconsForDataPool");
        q.setParameter("imageType", ImageType.Icon);
        q.setParameter("clientId", client.getId());
        return (List<MlsImage>) q.getResultList();
    }

    @RolesAllowed(value = {"MasterAdmin", "Admin", "User"})
    public MlsImage getImage(int imageID) throws NonExistingObjectException, InsufficientAccessRightException {
        MlsImage i = em.find(MlsImage.class, imageID);
        if (i == null) {
            throw new NonExistingObjectException();
        }
        mlsUser u = userManager.getCurrentUser();
        if (!u.getClients().contains(i.getClient())
                || u.getMaxConfidentiality(i.getClient()).compareTo(i.getConfidentiality()) < 0
                || i.getPrivateAccess()) {
            throw new InsufficientAccessRightException();
        }
        return i;
    }

    @Override
    public void deleteUnusedIcons(List<MlsImage> icons) {
        List<MlsImage> toBeRemoved = new ArrayList<>();
        for (MlsImage icon : icons) {
            if (!icon.getRelatives().isEmpty()) {
                toBeRemoved.add(icon);
            }
        }
        for (Iterator it = toBeRemoved.iterator(); it.hasNext();) {
            MlsImage icon = (MlsImage) it.next();
            objectManager.removeWithoutMessage(icon);
            it.remove();
        }
    }

    @Override
    public void ensureDefaultWSMIconSet(int clientId) {
        mlsClient c = em.find(mlsClient.class, clientId);
        List<MlsImage> icons = getIcons(c);
        List<String> existingNames = new ArrayList<>();
        icons.stream().forEach(i -> {
            existingNames.add(i.getHeadline());
        }
        );
        List<ImageIcon> iconImages = new ArrayList<>();
        List<String> missingNames = new ArrayList<>(MlIconLoader.getWorkSphereMapIconNames());
        missingNames.stream().filter(s -> !existingNames.contains(s)).forEach(m -> {
            Image ii = MlIconLoader.getWorkSphereMapIcon(m);
            if (m != null) {
                ImageIcon imageIcon = new ImageIcon(ii);
                // setting the description here just to be used as headline when creating the MlsImage
                imageIcon.setDescription(m);
                iconImages.add(imageIcon);
            }
        });
        addIconImages(c, iconImages);
    }
}
