/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.importer.maps;

import com.mindliner.gui.PowerpointImportDialog;
import com.mindliner.gui.PowerpointImportPanel;
import com.mindliner.gui.ProgressDialog;
import com.mindliner.thread.SimpleSwingWorker;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;

/**
 *
 * @author Dominic Plangger
 */
public class PowerpointImportWorker extends SimpleSwingWorker {

    private ProgressDialog progressDialog = null;
    private MlPowerpointImporter importer;
    private File file;
    private boolean isXmlPpt;

    public PowerpointImportWorker(MlPowerpointImporter importer, File file, boolean isXmlPpt) {
        this.importer = importer;
        this.file = file;
        this.isXmlPpt = isXmlPpt;
    }

    @Override
    protected Object doInBackground() throws Exception {
        progressDialog = new ProgressDialog();
        progressDialog.setVisible(true);
        try {
            PowerpointImportPanel panel = new PowerpointImportPanel();
            panel.setImporter(importer);

            if (isXmlPpt) {
                XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(file));
                panel.setXmlSlideShow(ppt);
            } else {
                SlideShow ppt = new SlideShow(new FileInputStream(file));
                panel.setSlideShow(ppt);
            }

            PowerpointImportDialog dialog = new PowerpointImportDialog(panel);
            progressDialog.setVisible(false);
            dialog.setVisible(true);
            
        } catch (Exception ex) {
            Logger.getLogger(MlPowerpointXmlImporter.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Unexpected error while analyzing Powerpoint file", "Powerpoint Import Error", JOptionPane.ERROR_MESSAGE);
            progressDialog.setVisible(false);
        }

        return null;
    }
}
