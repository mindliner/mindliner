/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.news;

import com.mindliner.view.containermap.ContainerMap;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javax.swing.JPanel;

/**
 *
 * @author Marius Messerli
 */
public class NewsContainer extends JPanel {

    private NewsPaneController controller = null;
    final JFXPanel fxPanel = new JFXPanel();

    public void initialize() {

        if (controller == null) {
            add(fxPanel);
        }

        Platform.runLater(() -> {
            if (controller == null) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    AnchorPane ap = fxmlLoader.load(getClass().getResource("NewsPane.fxml").openStream());
                    controller = (NewsPaneController) fxmlLoader.getController();
                    ap.setPrefHeight(getPreferredSize().height);
                    ap.setPrefWidth(getPreferredSize().width);
                    Scene s = new Scene(ap);
                    s.getStylesheets().add(getClass().getResource("/com/mindliner/styles/desktopDefault.css").toExternalForm());
                    fxPanel.setScene(s);
                    setLayout(new BorderLayout());
                    add(fxPanel, BorderLayout.CENTER);
                } catch (IOException ex) {
                    Logger.getLogger(ContainerMap.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

}
