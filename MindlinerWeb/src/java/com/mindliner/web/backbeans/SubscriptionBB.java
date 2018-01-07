/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.mindliner.categories.MlsEventType;
import com.mindliner.categories.MlsEventType.EventType;
import com.mindliner.categories.NewsAggregation.Grouping;
import com.mindliner.entities.MlsNews;
import com.mindliner.entities.MlsSubscription;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.managers.ObjectManagerLocal;
import com.mindliner.managers.SubscriptionManagerLocal;
import com.mindliner.managers.UserManagerLocal;
import com.mindliner.managers.UserManagerRemote;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import org.primefaces.component.tabview.TabView;

/**
 *
 *
 * @author Marius Messerli
 */
@ManagedBean
@SessionScoped
public class SubscriptionBB {

    // the current event-specific subscription
    private MlsSubscription currentEventSubscription = null;

    private EventType eventType = EventType.Any;
    private mlsUser actor = null;
    private boolean reverse = false;
    private List<MlsNews> news = new ArrayList<>();
    private Date lastNewsUpdate = null;
    private Grouping sortOrder = Grouping.ByDay;
    private final static String NEWS_MESSAGE_KEY_SUFFIX = "NewsMessage";
    private final static int SUBSCRIPTIONS_TAB_INDEX = 1;
    
    // cache subscribed property for rendered queries
    private mlsObject object;
    private boolean subscribed;
    
    private int numNewsRows = 25;

    @EJB
    private SubscriptionManagerLocal sm;

    @EJB
    ObjectManagerLocal om;

    @EJB
    private UserManagerLocal um;
    @EJB
    private UserManagerRemote userManagerRemote;

    public SubscriptionBB() {
    }
    
    private TabView tabView = new TabView();

    public TabView getTabView () {
        return tabView;
    }

    private void setTabView(TabView tabView ) {
        this.tabView = tabView;
    }

    public void subscribe(mlsObject object) {
        this.object = object;
        this.subscribed = true;
        sm.subscribe(EventType.Any, object, null, false);
    }

    public void unsubscribe(mlsObject object) {
        this.object = object;
        this.subscribed = false;
        sm.deleteAllSubscriptions(object);
    }

    public boolean isSubscribed(mlsObject object) {
        if (this.object== null || this.object.getId() != object.getId()) {
            this.object = object;
            subscribed = sm.isSubscribed(object, EventType.Any, null);
        }
        return subscribed;
    }

    public List<MlsSubscription> getSubscriptions() {
        return sm.getAllSubscriptions();
    }

    public MlsSubscription getCurrentEventSubscription() {
        return currentEventSubscription;
    }

    public void setCurrentEventSubscription(MlsSubscription currentEventSubscription) {
        this.currentEventSubscription = currentEventSubscription;
    }

    public void deleteAllSubscriptions() {
        sm.deleteAllSubscriptions();
        this.tabView.setActiveIndex(SUBSCRIPTIONS_TAB_INDEX);
    }

    public void deleteSubscription(MlsSubscription s) {
        if (s != null) {
            try {
                sm.deleteSubscription(s.getId());
            } catch (ForeignOwnerException ex) {
                Logger.getLogger(SubscriptionBB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.tabView.setActiveIndex(SUBSCRIPTIONS_TAB_INDEX);
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public mlsUser getActor() {
        return actor;
    }

    public void setActor(mlsUser actor) {
        this.actor = actor;
    }

    public int getNumNewsRows() {
        return numNewsRows;
    }

    public void setNumNewsRows(int numNewsRows) {
        this.numNewsRows = numNewsRows;
    }
    
    public boolean isReverseSubscription() {
        return reverse;
    }

    public void setReverseSubscription(boolean reverseSubscription) {
        this.reverse = reverseSubscription;
    }

    public List<EventType> getEventTypes() {
        if (um.isInRole("MasterAdmin")) {
            return Arrays.asList(EventType.values());
        } else {
            return MlsEventType.getUserEventTypes();
        }
    }

    public List<mlsUser> getActors() {
        return userManagerRemote.getUsersWithSharedDatapool(um.getCurrentUser().getId());
    }

    public void createSubscription() {
        sm.subscribe(eventType, null, actor, reverse);
        this.tabView.setActiveIndex(SUBSCRIPTIONS_TAB_INDEX);
    }

    public void specificActorCheckboxChanged(ValueChangeEvent e) {
        Boolean newState = (Boolean) e.getNewValue();
        if (newState != null) {
            if (newState == false) {
                actor = null;
            } else {
                actor = getActors().get(0);
            }
            this.tabView.setActiveIndex(SUBSCRIPTIONS_TAB_INDEX);
            FacesContext.getCurrentInstance().renderResponse();
        }
    }

    public boolean isActorSpecified() {
        return actor != null;
    }
    
    public void setActorSpecified(boolean actorSpecified) {
        //Do nothing, handled by specificActorCheckboxChanged, setter is still required for property 
    }

    public void deliver() {
        sm.deliverSubscriptions();
    }

    public List<MlsNews> getNews() {
        if (lastNewsUpdate == null || (new Date()).getTime() - lastNewsUpdate.getTime() > 60 * 1000) {
            news = sm.getNews();
            switch (sortOrder) {
                case ByActor:
                    sortNewsByActor();
                    break;
                case ByEvent:
                    sortNewsByEvent();
                    break;
                case ByDay:
                    sortNewsByDate();
                    break;
            }
            lastNewsUpdate = new Date();
        }
        return news;
    }
    
    public List<MlsNews> getNewsForDisplay() {
        if(news.size() > numNewsRows) {
            return news.subList(0, numNewsRows);
        }
        return news;
    }

    public void archiveNewsArticle(MlsNews article) {
        om.setArchived(article.getId(), true);
        news.remove(article);
    }
    
    public void archiveAllArticles(){
        om.bulkSetArchived(mlsObject.getIds((List) news), true);
        news.clear();
        lastNewsUpdate = new Date();
    }

    public String getObjectHeadline(MlsNews news) {
        if (news.getLog() == null) {
            return "N/A";
        }
        mlsObject o = om.findLocal(news.getLog().getObjectId());
        if (o == null) {
            return "N/A";
        }
        return o.getHeadline().isEmpty() ? "." : o.getHeadline();
    }

    public mlsObject getNewsObject(MlsNews news) {
        if (news.getLog() == null) {
            return null;
        }
        return om.findLocal(news.getLog().getObjectId());
    }

    public void sortNewsByEvent() {
        Collections.sort(news, new Comparator<MlsNews>() {

            @Override
            public int compare(MlsNews o1, MlsNews o2) {
                if (o1.getLog() == null) {
                    if (o2.getLog() == null) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    if (o2.getLog() == null) {
                        return 1;
                    } else {
                        return o1.getLog().getEventType().compareTo(o2.getLog().getEventType());
                    }
                }
            }
        });
        sortOrder = Grouping.ByEvent;
    }

    public void sortNewsByActor() {
        Collections.sort(news, new Comparator<MlsNews>() {

            @Override
            public int compare(MlsNews o1, MlsNews o2) {
                if (o1.getLog() == null) {
                    if (o2.getLog() == null) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    if (o2.getLog() == null) {
                        return 1;
                    } else {
                        return o1.getLog().getUser().getFirstName().compareTo(o2.getLog().getUser().getFirstName());
                    }
                }
            }
        });
    }

    /**
     * If both news articles have a link to a log record then compare the dates
     * of the underlying log record, otherwise compare the creation dates of the
     * news articles.
     */
    public void sortNewsByDate() {
        Collections.sort(news, new Comparator<MlsNews>() {

            @Override
            public int compare(MlsNews o1, MlsNews o2) {
                if (o1.getLog() == null || o2.getLog() == null) {
                    return o2.getCreationDate().compareTo(o1.getCreationDate());
                } else {
                    return o2.getLog().getTime().compareTo(o1.getLog().getTime());
                }
            }
        });

    }
    
    /**
     * Generates a formatted news message depending on the event type.
     * Generates a generic message when the affiliated log or object is not available.
     * 
     * @param news news entry for which the message is generated
     * @return 
     */
    public String getFormattedNewsMessage(MlsNews news) {
        
        if (news.getLog() == null) return "No Log";
        String key = news.getLog().getEventType().toString()+NEWS_MESSAGE_KEY_SUFFIX;
        
        String bundleString = com.mindliner.web.util.Messages.getStringFromBundle(key);
        mlsObject obj = getNewsObject(news);
        String message;
        if (obj != null) {
            if (!om.isAuthorizedForCurrentUser(obj)) {
                return "The accessibility of this object has changed and you are no longer allowed to view it.";
            }
            message = MessageFormat.format(bundleString, news.getLog().getUser().getUserName(), obj.getObjectTypeAsString(), obj.getHeadline(), 
                    obj.getClient().getName(), "workspace.xhtml?id="+obj.getId());
        } else {
            message = MessageFormat.format(bundleString, news.getLog().getUser().getUserName(), "", news.getLog().getHeadline(), "");
        }
        return message;
    }

}
