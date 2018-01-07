package com.mindliner.commands.bulk;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.MindlinerOnlineCommand;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.mlModifiedException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;

/**
 * Base class for the bulk update commands. The advantages of these commands
 * (relative to their single object counterparts) is performance as the
 * instructions are given to the server in a single RPC call rather than in a
 * series of individual calls.
 *
 * Note that the server will check each object before updating and only really
 * updates the object if the new state differes from the actual state.
 *
 * @author Marius Messerli
 */
public abstract class BulkUpdateCommand extends MindlinerOnlineCommand {

    List<mlcObject> objects = null;

    /**
     * Constructor
     *
     * @param candidates A list of objects submitted to this command. Only those
     * objects will be filtered through which match the factorizableClass
     * argument. Objects that do not belong to the client of the first object
     * are removed from the list in the constructor.
     */
    public BulkUpdateCommand(List<mlcObject> candidates) {
        super(candidates.get(0), false);
        this.objects = new ArrayList<>(candidates);
    }

    /**
     * Removes objects that are not factorizable by this bulk updater because
     * they do not possess the corresponding attribute(s).
     *
     * @param compatibleObjectClasses
     */
    public final void rejectIncompatibleObjects(List<Class> compatibleObjectClasses) {
        for (Iterator it = objects.iterator(); it.hasNext();) {
            if (!compatibleObjectClasses.contains(it.next().getClass())) {
                it.remove();
            }
        }
    }

    /**
     * Fetches the latest versions of the specified objects
     *
     * @param versions
     * @throws javax.naming.NamingException
     * @todo Implement this function and then replace all the bulk commands that
     * return an integer vector with versions
     */
    protected void updateObjectVersions(Map<Integer, Integer> versions) throws NamingException {
        for (mlcObject o : objects) {
            Integer version = versions.get(o.getId());
            if (version != null) {
                o.setVersion(versions.get(o.getId()));
            }
        }
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        for (mlcObject o : objects) {
            mapTemporaryObjectId(o);
        }
    }

    public List<mlcObject> getObjects() {
        return objects;
    }

}
