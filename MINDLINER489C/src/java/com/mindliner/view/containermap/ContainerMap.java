package com.mindliner.view.containermap;

import com.mindliner.clientobjects.MlcContainerMap;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.Colorizer;
import com.mindliner.events.ObjectChangeObserver;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.view.dispatch.MlObjectViewer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javax.swing.JPanel;

/**
 * ContainerMap implemented with a JavaFX panel integrated in a swing JPanel
 *
 * @author Dominic Plangger
 */
public class ContainerMap extends JPanel implements ObjectChangeObserver, MlObjectViewer {

    private List<mlcObject> objects = new ArrayList<>();
    private boolean active = true;
    private FXMLController controller = null;

    public ContainerMap() {

    }

    @Override
    public void objectChanged(mlcObject o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void objectDeleted(mlcObject o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void objectCreated(mlcObject o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void objectReplaced(int oldId, mlcObject o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void display(mlcObject object, ViewType type) {
        if (!active) {
            return;
        }
        display(java.util.Arrays.asList(object), type);
    }

    public void initialize() {
        final JFXPanel fxPanel = new JFXPanel();
        if (controller == null) {
            add(fxPanel);
        }

        addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                // the fxpanel's pref size is not adapted automatically.
                // we have to set it here otherwise the containermap size will not adjust with the surrounding tab size
                final Dimension size = e.getComponent().getSize();
                fxPanel.setPreferredSize(size);
                invalidate();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        Platform.runLater(() -> {
            if (controller == null) {
                try {
                    int w = ContainerMap.this.getWidth();
                    int h = ContainerMap.this.getHeight();
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setResources(ResourceBundle.getBundle("com/mindliner/resources/WorksphereMap"));
                    final StackPane ap = (StackPane) fxmlLoader.load(getClass().getResource("FXMLContainerMap.fxml").openStream());
                    controller = (FXMLController) fxmlLoader.getController();
                    initializeContainerProperties();
                    final Scene scene = new Scene(ap);
                    scene.getStylesheets().add(getClass().getResource("/com/mindliner/styles/desktopDefault.css").toExternalForm());
//                    ap.setBackground(new Background(new BackgroundFill(Color.AQUAMARINE, CornerRadii.EMPTY, Insets.EMPTY)));
                    ap.setPrefHeight(h);
                    ap.setPrefWidth(w);
                    fxPanel.setScene(scene);
                } catch (IOException ex) {
                    Logger.getLogger(ContainerMap.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            }
        });

    }

    private void initializeContainerProperties() {
        List<ContainerProperties> props = new ArrayList<>();
        props.add(new ContainerProperties(FixedKeyColorizer.FixedKeys.CONTAINER_APPLICATION, "Application"));
        props.add(new ContainerProperties(FixedKeyColorizer.FixedKeys.CONTAINER_ORGANIZATION, "Organization"));
        props.add(new ContainerProperties(FixedKeyColorizer.FixedKeys.CONTAINER_PROCESS, "Process"));
        props.add(new ContainerProperties(FixedKeyColorizer.FixedKeys.CONTAINER_DESCRIPTION, "Description"));
        controller.setContainerProps(props);
    }

    @Override
    public void display(List<mlcObject> objs, ViewType type) {
        //First object in the list has to be a container map object in order to fetch its nodes
        if (!active || objs.isEmpty() || !(objs.get(0) instanceof MlcContainerMap)) {
            return;
        }
        this.objects = objs;

        MlcContainerMap templ = (MlcContainerMap) objects.get(0);
        ContainerMapDisplayer displayer = new ContainerMapDisplayer(controller, templ);
        Thread th = new Thread(displayer);
        th.setDaemon(true);
        th.start();
    }

    @Override
    public boolean isSupported(ViewType type) {
        return ViewType.ContainerMap.equals(type);
    }

    @Override
    public void back() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

}
