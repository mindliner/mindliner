/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.main;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This task forces the application to quit after a grace period in which
 * the normal termination procedure runs.
 * 
 * @author Marius Messerli
 */
public class TerminationTask extends TimerTask{

    @Override
    public void run() {
        // send signal for abnormal termination
        Logger.getLogger(MindlinerMain.class.getName()).log(Level.SEVERE, "Mindliner failed to close in reasonable time; forcing termination now");
        System.exit(1);
    }
    
}
