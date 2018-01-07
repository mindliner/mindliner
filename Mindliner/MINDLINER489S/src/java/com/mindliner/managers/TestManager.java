/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.entities.EntityRefresher;
import com.mindliner.entities.MlsLink;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsKnowlet;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsRatingDetail;
import com.mindliner.entities.mlsUser;
import com.mindliner.enums.LinkRelativeType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * A class to generate a few dummy objects.
 *
 * @author Marius Messerli
 */
@DeclareRoles(value = {"Admin", "User", "MasterAdmin"})
@Stateless
public class TestManager implements TestManagerRemote {

    @PersistenceContext
    EntityManager em;
    @EJB
    UserManagerLocal userManager;
    @EJB
    CategoryManagerRemote catman;
    @EJB
    SolrServerBean solrServer;

    private static final String[] dummyHeadline = {
        "Lorem ipsum dolor sit amet, consectetur adipisicing elit", "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua",
        "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
        "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore",
        "eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident",
        "sunt in culpa qui officia deserunt mollit anim id est laborum.",
        "Curabitur pretium tincidunt lacus. Nulla gravida orci a odio",
        "Nullam varius, turpis et commodo pharetra, est eros bibendum",
        "elit, nec luctus magna felis sollicitudin mauris. Integer in mauris eu nibh",
        "euismod gravida. Duis ac tellus et risus vulputate vehicula. Donec lobortis risus",
        "a elit. Etiam tempor. Ut ullamcorper, ligula eu tempor congue, eros est euismod turpis",
        "id tincidunt sapien risus a quam. Maecenas fermentum consequat mi. Donec fermentum. Pellentesque",
        "malesuada nulla a mi. Duis sapien sem, aliquet nec, commodo eget, consequat quis, neque.",
        "Aliquam faucibus, elit ut dictum aliquet, felis nisl adipiscing sapien, sed malesuada diam lacus",
        "eget erat. Cras mollis scelerisque nunc. Nullam arcu. Aliquam consequat. Curabitur augue lorem",
        "dapibus quis, laoreet et, pretium ac, nisi. Aenean magna nisl, mollis quis, molestie eu, feugiat in, orci. In hac habitasse platea dictumst"};

    @RolesAllowed(value = {"MasterAdmin"})
    @Override
    public void addDummyContent(int recordCount, int parentId) {
        mlsObject parent = em.find(mlsObject.class, parentId);
        if (!(parent instanceof mlsKnowlet)) {
            System.err.println("parent must be a knowlet");
            return;
        }
        mlsKnowlet kparent = (mlsKnowlet) parent;

        // select random 5000 objects and link each newly created object to one of these 5000 objects.
        // in this way we can increase island sizes randomly
        Query q = em.createQuery("SELECT k FROM mlsKnowlet k WHERE k.client.id = " + parent.getClient().getId());
        List<mlsKnowlet> result = q.setMaxResults(5000).getResultList();
        Random rand = new Random(System.currentTimeMillis());
        mlsUser u = userManager.getCurrentUser();

        for (int i = 0; i < recordCount; i++) {
            mlsKnowlet k = new mlsKnowlet();
            k.setClient(parent.getClient());
            k.setConfidentiality(parent.getConfidentiality());
            int headlineIndex = (int) Math.max(0, Math.floor(Math.random() * dummyHeadline.length));
            k.setHeadline(dummyHeadline[headlineIndex]);
            k.setOwner(parent.getOwner());
            mlsRatingDetail rd = new mlsRatingDetail();
            k.setRatingDetail(rd);
            em.persist(rd);
            em.persist(k);
            em.flush();

            solrServer.addObject(k, false);

            mlsKnowlet tparent = result.get(rand.nextInt(result.size()));
            MlsLink link1 = new MlsLink(tparent, k, u);
            MlsLink link2 = new MlsLink(k, tparent, u);
            em.persist(link1);
            em.persist(link2);
            if (i % 200 == 0) {
                System.out.println(i);
                em.clear();
                solrServer.commit();
            }
        }
        em.flush();
        solrServer.commit();
    }

    @Override
    public void argumentTransport(mlsConfidentiality conf) {
        System.out.println("confidentiality is " + conf.getName());
    }


    @Override
    public void testUploadSpeed(int[] uploadArray) {
        System.out.println("Int array of size " + uploadArray.length + " received.");
    }

    @Override
    public int[] testDownloadSpeed(int arraySize) {
        if (arraySize > 0 || arraySize > 100000) {
            return new int[arraySize];
        } else {
            System.err.println("arraySize must be between 0 and 100,000)");
            return new int[1];
        }
    }

    @Override
    public void testPing() {
        System.out.println("ping received on " + new Date());
    }

    class SimpleLink {

        private mlsObject source;
        private mlsObject target;

        public SimpleLink(mlsObject source, mlsObject target) {
            this.source = source;
            this.target = target;
        }

        public mlsObject getSource() {
            return source;
        }

        public mlsObject getTarget() {
            return target;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.source);
            hash = 53 * hash + Objects.hashCode(this.target);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SimpleLink other = (SimpleLink) obj;
            if (!Objects.equals(this.source, other.source)) {
                return false;
            }
            if (!Objects.equals(this.target, other.target)) {
                return false;
            }
            return true;
        }

    }

    @Override
    public void reverseListOrder(int parentId) {
        Query q = em.createNamedQuery("MlsLink.getObjectRelatives");
        q.setParameter("id", parentId);
        q.setParameter("relativeType", LinkRelativeType.OBJECT);
        mlsObject o = em.find(mlsObject.class, parentId);
        if (o != null) {
            if (o.getRelatives().size() > 1) {
                mlsObject zero = o.getRelatives().get(0);
                o.getRelatives().remove(zero);
                // now insert the former zero position at one leaving the former one at zero
                o.getRelatives().add(1, zero);
                System.out.println("first and second relative swapped for " + o.getHeadline());
            } else {
                System.err.println("Object has less than two relatives");
            }
        } else {
            System.err.println("Object not found");
        }
    }

}
