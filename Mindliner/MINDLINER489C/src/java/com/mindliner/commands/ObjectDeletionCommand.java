/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import static com.mindliner.commands.MindlinerOnlineCommand.mapTemporaryObjectId;
import com.mindliner.connector.SftpConnector;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.MindlinerMain;
import com.mindliner.view.interaction.MapperMouseListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * This commmand deletes the specified object from cache and server.
 *
 * @author Marius Messerli
 */
public class ObjectDeletionCommand extends MindlinerOnlineCommand {

    private List<mlcObject> objects = null;
    private static final Pattern urlPattern = Pattern.compile(
            "\\b((?:sftp):/{1,2}[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])", Pattern.CASE_INSENSITIVE);

    public ObjectDeletionCommand(List<mlcObject> o) {
        super(null, true);
        objects = o;
        
        boolean wontDelete = false;
        Iterator<mlcObject> it = objects.iterator();
        while (it.hasNext()) {
            mlcObject curr = it.next();
            boolean foreignObject = !curr.getOwner().equals(CacheEngineStatic.getCurrentUser());
            boolean taskWithWorkUnits = (curr instanceof mlcTask && (!((mlcTask) curr).getWorkUnits().isEmpty()));
            if (foreignObject || taskWithWorkUnits) {
                wontDelete = true;
                it.remove();
            } else {
                CacheEngineStatic.removeObjectFromCache(curr);
            }
        }
        if (wontDelete) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), "One or several items were not deleted because they beloned to someone else or were tasks with accrued work", "Object Deletion", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void checkForSftpReferences(mlcObject curr) {
        // If the user deletes an object referencing files on a SFTP server,
        // we give the user the possibility to also remove the files on the server
        Matcher matcher = urlPattern.matcher(curr.getDescription());
        List<URI> uris = new ArrayList<>();
        while (matcher.find()) {
            try {
                int matchStart = matcher.start();
                int matchEnd = matcher.end();
                String subs = curr.getDescription().substring(matchStart, matchEnd);
                URI uri = new URI(subs);
                uris.add(uri);
            } catch (URISyntaxException ex) {
                Logger.getLogger(MapperMouseListener.class.getName()).log(Level.INFO, "Parsed url could not be converted into URL object", ex);
            }
        }
        if (!uris.isEmpty()) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), "You deleted an object containing a reference to a remote file. Follow procedure to delete the file too.", "SFTP Deletion", JOptionPane.INFORMATION_MESSAGE);
        }
        for (URI uri : uris) {
            SftpConnector.getInstance().removeFile(uri);
        }
    }

    public ObjectDeletionCommand(mlcObject o) {
        this(wrapObject(o));
    }

    private static List<mlcObject> wrapObject(mlcObject o) {
        List<mlcObject> list = new ArrayList<>();
        list.add(o);
        return list;
    }

    @Override
    public void execute() throws mlModifiedException, NamingException {
        if (isExecuted()) {
            throw new IllegalStateException("This command can only be executed once.");
        }
        for (mlcObject obj : objects) {
            checkForSftpReferences(obj);
            mapTemporaryObjectId(obj);
        }
        CacheEngineStatic.removeObjects(objects);
    }

    @Override
    public String toString() {
        return "Object Deletion (" + getFormattedId() + ")";
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String getDetails() {
        return "Deleting " + getFormattedId();
    }

    @Override
    protected String getFormattedId() {
        if (objects != null && !objects.isEmpty()) {
            StringBuilder ids = new StringBuilder();
            for (mlcObject object : objects) {
                ids.append(getFormattedId(object.getId()));
                ids.append(",");
            }
            ids.deleteCharAt(ids.length() - 1);
            return ids.toString();
        } else {
            return "N";
        }
    }

    public List<mlcObject> getDeletedObjects() {
        return objects;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ObjectDeletionCommand)) {
            return false;
        }
        ObjectDeletionCommand that = (ObjectDeletionCommand) obj;
        return objects.equals(that.getDeletedObjects());
    }

}
