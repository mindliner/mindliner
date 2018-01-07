/*
 * BaseFilter.java
 *
 * Created on 22. March 2007, 09:02
 *
 */
package com.mindliner.contentfilter;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.analysis.MlClassHandler.MindlinerObjectType;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.contentfilter.TimeFilter.TimePeriod;
import com.mindliner.contentfilter.evaluator.ClientEvaluator;
import com.mindliner.contentfilter.evaluator.ConfidentialityEvaluator;
import com.mindliner.contentfilter.evaluator.CustomImageEvaluator;
import com.mindliner.contentfilter.evaluator.ArchivedEvaluator;
import com.mindliner.contentfilter.evaluator.ForeignEvaluator;
import com.mindliner.contentfilter.evaluator.ModificationEvaluator;
import com.mindliner.contentfilter.evaluator.PrivacyEvaluator;
import com.mindliner.contentfilter.evaluator.TextEvaluator;
import com.mindliner.contentfilter.loaders.RecordLoader;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;

/**
 * This class serves as base class for display filters controlling which
 * mindliner objects get displayed and exported. The class and its subclass
 * implement the Template Method pattern to check if an object passes the
 * filter.
 *
 * This class has two engines that do the work: loaders and evaluators. The
 * loaders call add to add new elements to the filter list. The evaluators
 * supervise the add method to make sure all elements comply with the
 * evaluator's restrictions.
 *
 * Loaders and evaluators must be initialized before the filter can be used,
 * i.e. before the filter's load method is called.
 *
 * @author Marius Messerli
 */
public class BaseFilter implements Serializable {

    protected List<mlsObject> records = new ArrayList<>();
    private MlClassHandler.MindlinerObjectType objectType = MlClassHandler.MindlinerObjectType.Any;
    protected Map<Integer, mlsConfidentiality> maxConfidentialities = null;
    protected int maxNumberOfElements = 100;
    protected boolean showArchived = false;
    protected boolean showPrivate = false;
    protected TimePeriod maxModificationAge = TimePeriod.All;
    protected mlsUser currentUser = null;
    protected Date lastLogout = null;
    protected String textConstraint = "";
    protected int relationLevel = 0;
    protected SortingMode defaultSorting = SortingMode.Modification;
    // used to search cells
    protected double rangeMin;
    protected double rangeMax;
    private boolean numberSearchRange = false;
    private List<Integer> ownerIds = new ArrayList<>();
    private Set<Integer> dataPoolIds = new HashSet<>();

    /**
     * Evaluator remove non-conforming elements from the list.
     */
    protected ArrayList<ObjectEvaluator> evaluatorList = new ArrayList<>();
    /**
     * Loaders add elements to the list.
     */
    protected ArrayList<RecordLoader> loaderList = new ArrayList<>();

    public enum SortingMode {

        Creation, Modification, Rating, SearchRelevance
    }
    private static final long serialVersionUID = 19640205L;

    /**
     * First loads objects by executing all loaders, then evaluate hits by each
     * of the evaluators and clip the results to no more than the maximum number
     * of hits requested. The sort order is defined by the sequence and the
     * nature of the loaders.
     *
     * @param em The entity manager references.
     */
    public void load(EntityManager em) {
        clearRecords();
        Iterator it = loaderList.iterator();
        for (; it.hasNext();) {
            RecordLoader rl = (RecordLoader) it.next();
            rl.load(this, em);
        }
        removeNonconformingObjects();

        // make sure we only have up to the maximum number of elements in the filter
        if (records.size() > getMaximumNumberOfDisplayElements()) {
            ArrayList l2 = new ArrayList(maxNumberOfElements);
            for (int i = 0; i < maxNumberOfElements; i++) {
                l2.add(records.get(i));
            }
            records = l2;
        }
    }

    public List<Integer> getOwnerIds() {
        return ownerIds;
    }

    public void setOwnerIds(List<Integer> ownerIds) {
        this.ownerIds = ownerIds;
    }

    public Set<Integer> getDataPoolIds() {
        return dataPoolIds;
    }

    public void setDataPoolIds(Set<Integer> dataPoolIds) {
        this.dataPoolIds = dataPoolIds;
    }

    /**
     * Defines how many hits are maximally loaded.
     *
     * @param maxnumber The number of elements to be displayed max.
     */
    public void setMaximumNumberOfDisplayElements(int maxnumber) {
        maxNumberOfElements = maxnumber;
    }

    public int getMaximumNumberOfDisplayElements() {
        return maxNumberOfElements;
    }

    public void setRecords(List<mlsObject> l) {
        records = l;
    }

    public List<mlsObject> getRecords() {
        return records;
    }

    /**
     * Returns the confidentiality cap for each of the calling user's data pools
     *
     * @return A map with the data pool id as the key and the maximum
     * confidentiality as the value
     */
    public Map<Integer, mlsConfidentiality> getMaxConfidentialities() {
        return maxConfidentialities;
    }

    public void setMaxConfidentialities(Map<Integer, mlsConfidentiality> maxConfidentialities) {
        this.maxConfidentialities = maxConfidentialities;
    }

    public void setTextConstraint(String tc) {
        textConstraint = tc;
    }

    public String getTextConstraint() {
        return textConstraint;
    }

    public boolean isShowArchived() {
        return showArchived;
    }

    public void setShowArchived(boolean showArchived) {
        this.showArchived = showArchived;
    }

    public boolean isShowPrivateElements() {
        return showPrivate;
    }

    public void setShowPrivateElements(boolean s) {
        showPrivate = s;
    }

    public void setCurrentUser(mlsUser u) {
        currentUser = u;
    }

    public mlsUser getCurrentUser() {
        return currentUser;
    }

    public int getRelationLevel() {
        return relationLevel;
    }

    public void setRelationLevel(int relationLevels) {
        this.relationLevel = relationLevels;
    }

    public void addRecord(mlsObject o) {
        records.add(o);
    }

    public void removeRecord(mlsObject o) {
        records.remove(o);
    }

    /**
     * Removes all records from the filter.
     */
    public void clearRecords() {
        records.clear();
    }

    /**
     * Iterates through the records and removes any object that is not
     * conforming to any of the registered evaluators.
     *
     */
    public void removeNonconformingObjects() {

        Iterator recordIterator = records.iterator();
        while (recordIterator.hasNext()) {
            mlsObject o = (mlsObject) recordIterator.next();

            Iterator eit = evaluatorList.iterator();
            boolean clearToPass = true;

            while (eit.hasNext() && clearToPass == true) {
                ObjectEvaluator oe = (ObjectEvaluator) eit.next();
                if (oe.passesEvaluation(o) == false) {
                    clearToPass = false;
                }
            }
            if (!clearToPass) {
                recordIterator.remove();
            }
        }
    }

    /**
     * This method adds a new evaluator to the list. If there is already an
     * evaluator of the same type it is replaced by the specified evaluator. If
     * no evaluator of the specified type is present then the new evaluator is
     * added to the list.
     */
    public void registerEvaluator(ObjectEvaluator evaluator) {
        ObjectEvaluator found = null;

        for (ObjectEvaluator eval : evaluatorList) {
            if (eval.getClass() == evaluator.getClass()) {
                found = eval;
            }
        }
        if (found != null) {
            if (evaluator.isMultipleInstancesAllowed() == false) {
                evaluatorList.remove(found);
            }
        }
        evaluatorList.add(evaluator);
    }

    public void unregisterEvaluator(ObjectEvaluator evaluator) {
        evaluatorList.remove(evaluator);
    }

    /**
     * Removes all evaluators of the specified class.
     *
     * @param c The evaluator class for which all evaluators are to be removed.
     */
    public void unregisterEvaluator(Class c) {
        Iterator it = evaluatorList.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o.getClass().equals(c)) {
                it.remove();
            }
        }
    }

    /**
     * Newly initialize the list and removes any previously registered
     * evaluator.
     */
    public void unregisterAllEvaluators() {
        evaluatorList = new ArrayList<>();
    }

    /**
     * This function adds the specified loader to the loader list ensuring that
     * only one loadder of the specified class exists in the list.
     *
     * @param loader
     */
    public void registerLoader(RecordLoader loader) {
        Iterator it = loaderList.iterator();
        while (it.hasNext()) {
            RecordLoader rl = (RecordLoader) it.next();
            if (rl.getClass() == loader.getClass()) {
                it.remove();
            }
        }
        loaderList.add(loader);
    }

    public void unregisterLoader(RecordLoader loader) {
        loaderList.remove(loader);
    }

    public void unregisterAllLoaders() {
        loaderList.clear();
    }

    public void setDefaultSorting(SortingMode ds) {
        defaultSorting = ds;
    }

    public SortingMode getDefaultSorting() {
        return defaultSorting;
    }

    public MindlinerObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(MindlinerObjectType type) {
        this.objectType = type;
    }

    public void setLastLogin(Date llt) {
        lastLogout = llt;
    }

    public Date getLastLogin() {
        return lastLogout;
    }

    /**
     * This function re-builds the evaluators based on the constraint settings
     * that were previously defined.
     */
    public void reconfigureEvaluators() {
        unregisterAllEvaluators();
        registerEvaluator(new ClientEvaluator(currentUser.getClients()));
        registerEvaluator(new ConfidentialityEvaluator(getMaxConfidentialities(), getCurrentUser()));
        registerEvaluator(new CustomImageEvaluator());
        registerEvaluator(new PrivacyEvaluator(isShowPrivateElements()));
        registerEvaluator(new ForeignEvaluator(getOwnerIds()));
        registerEvaluator(new ArchivedEvaluator(isShowArchived()));

        if (getMaxModificationAge() != TimePeriod.All) {
            registerEvaluator(new ModificationEvaluator(currentUser, getMaxModificationAge()));
        }

        registerEvaluator(new TextEvaluator(textConstraint, relationLevel));
    }

    public TimePeriod getMaxModificationAge() {
        return maxModificationAge;
    }

    public void setMaxModificationAge(TimePeriod maxModificationAge) {
        this.maxModificationAge = maxModificationAge;
    }

    public double getRangeMin() {
        return rangeMin;
    }

    public void setRangeMin(double rangeMin) {
        this.rangeMin = rangeMin;
        numberSearchRange = true;
    }

    public double getRangeMax() {
        return rangeMax;
    }

    public void setRangeMax(double rangeMax) {
        this.rangeMax = rangeMax;
        numberSearchRange = true;
    }

    public boolean isNumberSearchRange() {
        return numberSearchRange;
    }
    
    
}
