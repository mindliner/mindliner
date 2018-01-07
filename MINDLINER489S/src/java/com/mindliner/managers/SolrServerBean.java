/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.MlsNews;
import com.mindliner.entities.Release;
import com.mindliner.entities.mlsContact;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsTask;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.ContentStreamBase;

/**
 *
 * @author Dominic Plangger
 */
@Singleton
public class SolrServerBean {

    private static final String SOLR_SERVER_URL = "http://localhost:8983/solr/";
    private static final String SOLR_CORE_PROD = "mindliner_core";
    private static final String SOLR_CORE_DEV = "mindliner_core_dev";

    private SolrServer server;

    @PostConstruct
    public void setup() {
        // Use a different SOLR server in dev mode (On production server, we have a prod and a dev mindliner instance running)
        server = new HttpSolrServer(SOLR_SERVER_URL + (Release.isDevelopmentState() ? SOLR_CORE_DEV : SOLR_CORE_PROD));
    }

    // @todo implement support for ConcurrentUpdateSolrServer
    public SolrServer getServer() {
        return server;
    }

    /**
     * Asynchronously removes the object.
     *
     * @param obj
     * @param commit The remove action becomes only visible after a commit
     */
    @Asynchronous
    public void removeObject(mlsObject obj, boolean commit) {
        try {
            String id = String.valueOf(obj.getId());
            // delete object itself
            server.deleteById(id);
            // delete any indexed files that are attached to the object
            server.deleteByQuery("attached_id:" + id);
            if (commit) {
                commit();
            }
        } catch (SolrServerException | IOException ex) {
            Logger.getLogger(SolrServerBean.class.getName()).log(Level.SEVERE, "Failed to remove object with ID [" + obj.getId() + "]", ex);
        }
    }

    @Asynchronous
    public void addFile(InputStream is, int attachedObjId) {
        if (is == null) {
            throw new IllegalArgumentException("Input stream cannot be null and ID must be real");
        }
        ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");
        InputContentStream stream = new InputContentStream(is);
        up.addContentStream(stream);
        up.setParam("literal.id", UUID.randomUUID().toString());
        up.setParam("literal.attached_id", String.valueOf(attachedObjId));

        up.setAction(AbstractUpdateRequest.ACTION.COMMIT, false, false);

        try {
            server.request(up);
        } catch (SolrServerException | IOException ex) {
            Logger.getLogger(SolrServerBean.class.getName()).log(Level.SEVERE, "Failed to add file to solr", ex);
        }
    }

    /**
     * Asynchronously adds all objects to the solr server. Automatically commits
     * after all objects have been added.
     *
     * @param objs
     */
    @Asynchronous
    public void addObjects(List<mlsObject> objs) {
        for (mlsObject obj : objs) {
            addObject(obj, false);
        }
        commit();
    }

    /**
     * Asynchronously initiates a soft commit (near-realtime commit)
     */
    @Asynchronous
    public void commit() {
        try {
            server.commit(false, false, true);
        } catch (IOException | SolrServerException ex) {
            Logger.getLogger(SolrServerBean.class.getName()).log(Level.SEVERE, "Failed to do solr commit", ex);
        }
    }

    /**
     * Asynchronously adds the given object to the solr server. Can also be used
     * for updating an object. The solr index can only remove and re-add an
     * object but not update. Therefore an update operation is identical to an
     * add operation.
     *
     * @param obj
     * @param commit The object becomes only visible to searches after a commit
     * (but commit is a heavy operation).
     */
    @Asynchronous
    public void addObject(mlsObject obj, boolean commit) {
        // we are not interested in news articles as they are derived from objects
        if (obj instanceof MlsNews) {
            return;
        }
        try {
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", obj.getId());
            if (obj.getHeadline() != null && !obj.getHeadline().isEmpty()) {
                doc.addField("headline", obj.getHeadline());
            }

            if (obj.getDescription() != null && !obj.getDescription().isEmpty()) {
                doc.addField("description", obj.getDescription());
            }
            doc.addField("owner_id", obj.getOwner().getId());
            doc.addField("client_id", obj.getClient().getId());
            doc.addField("modification", obj.getModificationDate());
            doc.addField("private", obj.getPrivateAccess());
            doc.addField("archived", obj.isArchived());
            doc.addField("creation_date", obj.getCreationDate());
            doc.addField("dtype", obj.getDiscriminatorValue());
            doc.addField("clevel", obj.getConfidentiality().getClevel());

            if (obj instanceof mlsTask) {
                mlsTask t = (mlsTask) obj;
                doc.addField("completed", t.isCompleted());
            } else if (obj instanceof mlsContact) {
                mlsContact co = (mlsContact) obj;
                doc.addField("firstname", co.getFirstName());
                doc.addField("lastname", co.getLastName());
            }
            server.add(doc);
            if (commit) {
                commit();
            }
        } catch (SolrServerException | IOException ex) {
            // @todo In case of an add failure we have different data sets in the Mindliner DB and the Soler server
            // This should be avoided. Suggestion: insert ID of the object into the DB to keep track
            // of which objects are not in the Solr Server.
            Logger.getLogger(SolrServerBean.class.getName()).log(Level.SEVERE, "Failed to add new object with ID [" + obj.getId() + "]", ex);
        }
    }

    private class InputContentStream extends ContentStreamBase {

        private final InputStream stream;

        public InputContentStream(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public InputStream getStream() throws IOException {
            return stream;
        }

    }
}
