/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.news;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.MlCacheException;
import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcUser;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * A wrapper class for a news object to be used in a FXML pane.
 *
 * @author Marius Messerli
 */
public class NewsArticle {

    private SimpleStringProperty headline = new SimpleStringProperty();
    private SimpleStringProperty type = new SimpleStringProperty();
    private SimpleStringProperty event = new SimpleStringProperty();
    private SimpleStringProperty actor = new SimpleStringProperty();
    private SimpleBooleanProperty archived = new SimpleBooleanProperty();
    private SimpleStringProperty timestamp = new SimpleStringProperty();

    private mlcNews news;

    public NewsArticle(mlcNews news) {
        try {
            this.news = news;
            headline.set(news.getHeadline());
            type.set(news.getNewsType().getName());
            archived.set(news.isArchived());
            event.set(news.getLog().getEventType().name());
            timestamp.set(new SimpleDateFormat().format(news.getLog().getTime()));
            mlcUser u = CacheEngineStatic.getUser(news.getLog().getUserId());
            if (u != null) {
                actor.set(u.getFirstName().concat(" ").concat(u.getLastName()));
            }
        } catch (MlCacheException ex) {
            Logger.getLogger(NewsArticle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public SimpleStringProperty getHeadlineProperty() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline.set(headline);
    }

    public String getHeadline() {
        return headline.get();
    }

    public SimpleBooleanProperty getArchivedProperty() {
        return archived;
    }

    public boolean isArchived() {
        return archived.get();
    }

    public void setArchived(boolean state) {
        archived.set(state);
    }

    public SimpleStringProperty getTypeProperty() {
        return type;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public SimpleStringProperty getEventProperty() {
        return event;
    }

    public String getEvent() {
        return event.get();
    }

    public void setEvent(String event) {
        this.event.set(event);
    }

    public SimpleStringProperty getActorProperty() {
        return actor;
    }

    public String getActor() {
        return actor.get();
    }

    public void setActor(String actor) {
        this.actor.set(actor);
    }

    public SimpleStringProperty getTimestampProperty() {
        return timestamp;
    }

    public String getTimestamp() {
        return timestamp.get();
    }

    public void setTimestamp(String time) {
        timestamp.set(time);
    }

    public mlcNews getNews() {
        return news;
    }

}
