/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.news;

import com.ibm.icu.util.Calendar;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.MlCacheException;
import com.mindliner.categories.NewsAggregation.Grouping;
import static com.mindliner.categories.NewsAggregation.Grouping.ByActor;
import static com.mindliner.categories.NewsAggregation.Grouping.ByDay;
import static com.mindliner.categories.NewsAggregation.Grouping.ByEvent;
import static com.mindliner.categories.NewsAggregation.Grouping.None;
import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.bulk.ArchiveBulkUpdateCommand;
import com.mindliner.entities.Release;
import com.mindliner.gui.MindlinerObjectDeletionHandler;
import com.mindliner.main.BrowserLauncher;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import java.awt.EventQueue;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import javax.swing.JOptionPane;

/**
 * FXML Controller for the news reader pane in Mindliner. Supports news grouping
 * and archiving of selected news articles.
 *
 * @author Marius Messerli
 */
public class NewsPaneController implements Initializable {

    // update the news pane every 5 minutes
    private static final int updateSeconds = 300;

    @FXML
    private TreeTableView<NewsArticle> newsTable;

    @FXML
    private TreeTableColumn<NewsArticle, String> headline;

    @FXML
    private TreeTableColumn<NewsArticle, String> type;

    @FXML
    private TreeTableColumn<NewsArticle, String> event;

    @FXML
    private TreeTableColumn<NewsArticle, String> actor;

    @FXML
    private TreeTableColumn<NewsArticle, String> timestamp;

    @FXML
    public ComboBox<Grouping> grouping;

    @FXML
    private Button archiveButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button manageSubscriptionButton;

    @FXML
    private ContextMenu newsContextMenu;

    TreeItem<NewsArticle> root = new TreeItem<>();
    private List<mlcNews> news = null;
    private Date lastNewsUpdate = new Date();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        grouping.getItems().addAll(Grouping.values());
        grouping.setValue(Grouping.ByActor);
        headline.setCellValueFactory(new TreeItemPropertyValueFactory<>("headline"));
        type.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));
        event.setCellValueFactory(new TreeItemPropertyValueFactory<>("event"));
        actor.setCellValueFactory(new TreeItemPropertyValueFactory("actor"));
        timestamp.setCellValueFactory(new TreeItemPropertyValueFactory("timestamp"));
        setupTimers();
        setupNewsTable();
    }

    private void setupTimers() {
        // DEFINE THE TMER FOR THE LIST UPDATE
        Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(updateSeconds / 60), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                news = CacheEngineStatic.getNews();
                lastNewsUpdate = new Date();
                rebuildView();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void viewLogObject(NewsArticle na) {
        mlcObject logObject = na.getNews().getLog() == null ? null : CacheEngineStatic.getObject(na.getNews().getLog().getObjectId());
        if (logObject != null) {
            EventQueue.invokeLater(() -> {
                MlViewDispatcherImpl.getInstance().display(logObject, MlObjectViewer.ViewType.Map);
            });
        }
    }

    @FXML
    private void viewSelectedLogObject() {
        NewsArticle na = newsTable.getSelectionModel().getSelectedItem().getValue();
        viewLogObject(na);
    }

    private void setupNewsTable() {

        newsTable.setRoot(root);
        newsTable.getSelectionModel().setCellSelectionEnabled(false);
        newsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // keys pressed
        EventHandler keyListener = new EventHandler<KeyEvent>() {
            public void handle(final KeyEvent keyEvent) {
                if (newsTable != null && newsTable.getSelectionModel().getSelectedItem() != null) {
                    NewsArticle na = newsTable.getSelectionModel().getSelectedItem().getValue();
                    switch (keyEvent.getCode()) {
                        case V:
                            viewLogObject(na);
                            break;
                    }
                }
            }

        };
        newsTable.setOnKeyPressed(keyListener);

        // selection changes
        newsTable.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TreeItem<NewsArticle>>() {
            @Override
            public void onChanged(Change<? extends TreeItem<NewsArticle>> change) {
                int selectionCount = newsTable.getSelectionModel().getSelectedIndices().size();
                if (selectionCount == 0) {
                    archiveButton.setDisable(true);
                    deleteButton.setDisable(true);
                } else {
                    archiveButton.setDisable(false);
                    archiveButton.setText("Archive Selected (" + selectionCount + ")");
                    deleteButton.setDisable(false);
                    deleteButton.setText("Delete Selected (" + selectionCount + ")");
                }
            }
        });

        // initialize with data
        news = CacheEngineStatic.getNews();
        lastNewsUpdate = new Date();

        rebuildView();
    }

    public void swapZeroColumn(TreeTableColumn newZeroColumn) {
        ObservableList cols = newsTable.getColumns();
        List<TreeTableColumn<NewsArticle, ?>> newOrderCols = new ArrayList<>(cols);
        int swapIndex = cols.indexOf(newZeroColumn);
        if (swapIndex != 0) {
            TreeTableColumn oldZeroCol = newOrderCols.get(0);
            newOrderCols.set(0, newZeroColumn);
            newOrderCols.set(swapIndex, oldZeroCol);
            newsTable.getColumns().setAll(newOrderCols);
        }
        newsTable.getSelectionModel().clearSelection();

    }

    private void adaptColumnPositionToGrouping() {
        switch (grouping.getValue()) {
            case ByActor:
                swapZeroColumn(actor);
                break;

            case ByEvent:
                swapZeroColumn(event);
                break;

            case ByDay:
                swapZeroColumn(timestamp);
                break;

            case None:
                swapZeroColumn(actor);
                break;
        }
    }

    public void display(List<mlcObject> objects) {
        news = (List) objects;
        lastNewsUpdate = new Date();
        rebuildView();
    }

    @FXML
    private void rebuildView() {
        if (news == null) {
            return;
        }
        if (news.isEmpty()) {
            root.getChildren().clear();
            return;
        }
        Map<Integer, TreeItem> topLevelMap = new HashMap<>();
        root.getChildren().clear();
        try {
            for (mlcObject o : news) {
                mlcNews n = (mlcNews) o;
                TreeItem<NewsArticle> newItem = null;
                switch (grouping.getValue()) {
                    case ByActor:
                        if (n.getLog() == null) {
                            System.err.println("news record has no associated log record");
                        } else if (topLevelMap.get(n.getLog().getUserId()) == null) {
                            // create new top level child
                            mlcUser u = CacheEngineStatic.getUser(n.getLog().getUserId());
                            newItem = new TreeItem<>(new NewsArticle(n));
                            topLevelMap.put(u.getId(), newItem);
                            root.getChildren().add(newItem);
                        } else {
                            newItem = topLevelMap.get(n.getLog().getUserId());
                            newItem.getChildren().add(new TreeItem<>(new NewsArticle(n)));
                        }

                        break;

                    case ByEvent:
                        if (topLevelMap.get(n.getLog().getEventType().ordinal()) == null) {
                            // create new top level child
                            newItem = new TreeItem<>(new NewsArticle(n));
                            topLevelMap.put(n.getLog().getEventType().ordinal(), newItem);
                            root.getChildren().add(newItem);
                        } else {
                            newItem = topLevelMap.get(n.getLog().getEventType().ordinal());
                            newItem.getChildren().add(new TreeItem<>(new NewsArticle(n)));
                        }
                        break;

                    case ByDay:
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(n.getLog().getTime());
                        int day = cal.get(Calendar.DAY_OF_YEAR);
                        if (topLevelMap.get(day) == null) {
                            // create new top level child
                            newItem = new TreeItem<>(new NewsArticle(n));
                            topLevelMap.put(day, newItem);
                            root.getChildren().add(newItem);
                        } else {
                            newItem = topLevelMap.get(day);
                            newItem.getChildren().add(new TreeItem<>(new NewsArticle(n)));
                        }
                        break;

                    case None:
                        root.getChildren().add(new TreeItem<>(new NewsArticle(n)));
                        break;

                    default:
                        throw new AssertionError();
                }
            }
            adaptColumnPositionToGrouping();
        } catch (MlCacheException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void archiveSelectedNewsArticles() {
        ObservableList<TreeItem<NewsArticle>> selectedItems = newsTable.getSelectionModel().getSelectedItems();
        if (selectedItems != null) {
            List<mlcNews> selectedNews = new ArrayList<>();
            for (TreeItem<NewsArticle> n : selectedItems) {
                selectedNews.add(n.getValue().getNews());
            }
            CommandRecorder cr = CommandRecorder.getInstance();
            cr.scheduleCommand(new ArchiveBulkUpdateCommand((List) selectedNews, true));
            news.removeAll(selectedNews);
            rebuildView();
        }
    }

    @FXML
    private void deleteSelectedNewsArticles() {
        ObservableList<TreeItem<NewsArticle>> selectedItems = newsTable.getSelectionModel().getSelectedItems();
        List<mlcNews> selectedNews = new ArrayList<>();
        for (TreeItem<NewsArticle> n : selectedItems) {
            selectedNews.add(n.getValue().getNews());
        }
        MindlinerObjectDeletionHandler.delete((List) selectedNews);
        news.removeAll(selectedNews);
        rebuildView();

    }

    @FXML
    private void manageSubscriptions() {
        try {
            BrowserLauncher.openWebpage(new URL(Release.DEFAULT_SUBSCRIPTION_ADMIN_PAGE));
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Cannot Open Mindliner Web", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    public void selectAll() {
        newsTable.getSelectionModel().selectAll();
    }

    @FXML
    public void deselectAll() {
        newsTable.getSelectionModel().clearSelection();
    }

}
