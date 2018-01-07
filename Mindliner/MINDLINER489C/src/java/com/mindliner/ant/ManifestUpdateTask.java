/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.ant;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Used template from http://stackoverflow.com/questions/7763735/how-do-i-read-in-an-existing-jars-manifest-and-append-to-its-classpath-using-a
 * 
 * Ant task that updates the classpath of an existing manifest with a certain string. 
 * Is for example used to add a reference to the glassfish client libraries (gf-client.jar) in standalone distribution
 * 
 * @author Dominic Plangger
 */
public class ManifestUpdateTask extends Task {

    private String append;
    private String property;
    private String directory;
    private String jar;
    
    @Override
    public void execute() throws BuildException {
        JarFile jarFile = null;
        Manifest manifest;

        try {
            jarFile = new JarFile(directory + "/" + jar);
            manifest = jarFile.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            String currClasspath = attributes.getValue("Class-Path");

            String newClasspath = currClasspath.concat(" ").concat(append);

            getProject().setProperty(property, newClasspath);
        } catch (IOException e) {
            System.out.println(e);
            throw new BuildException();
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ex) {
                    Logger.getLogger(ManifestUpdateTask.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    public void setAppend(String append) {
        this.append = append;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setDir(String directory) {
        this.directory = directory;
    }

    public void setJar(String jar) {
        this.jar = jar;
    }
    
}
