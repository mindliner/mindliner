/*
 * StatusBar.java
 *
 * Created on 4. November 2007, 21:40
 */
package com.mindliner.gui;

import com.mindliner.commands.CommandRecorder;
import com.mindliner.entities.Colorizer;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.main.MindlinerMain;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.StatusReporter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Marius Messerli
 */
public class StatusBar extends JPanel implements StatusReporter {

    private boolean cancelWasPressed = false;
    public static final int INDETERMINATE = -1;
    private CommandEditorImpl commandEditor = null;

    public StatusBar() {
        initComponents();
        configureComponents();
        done();
    }

    private void configureComponents() {
        Timer t = new Timer("Mindliner Command Queue Indicator");
        t.schedule(new QueueIndicatorUpdater(), new Date(), 1000);
    }

    public void colorizeComponents() {
        BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        setBackground(fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_BACKGROUND));
        Color fg = fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT);
        Color bg = fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_BACKGROUND);
        QueueCommandLabel.setForeground(fg);
        MessageLabel.setForeground(fg);
        ProgressBar.setBackground(bg);
        ProgressBar.setForeground(fg);
        QueueProgress.setForeground(fg);
        QueueProgress.setBackground(bg);
    }

    @Override
    public void setMessage(String m) {
        System.out.println(new Date() + ": " + m);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MessageLabel.setText(m);
            }
        });
    }

    @Override
    public void setMaximum(int max) {
        ProgressBar.setMaximum(max);
    }

    /**
     * Sets the progress text and updates the progress bar.
     *
     * @param m The message to be displayed.
     * @param p The progress.
     */
    @Override
    public void setMessage(String m, int p) {
        setMessage(m);
        setProgress(p);
    }

    /**
     * Hides the progress bar and the cancel button and re-sets the range for
     * the progress bar to 0..100.
     */
    @Override
    public void done() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ProgressBar.setVisible(false);
                ProgressBar.setMinimum(0);
                ProgressBar.setMaximum(100);
                ProgressBar.setIndeterminate(false);
                MessageLabel.setText("");
            }
        });
    }

    @Override
    public void setProgress(int p) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (ProgressBar.isVisible() == false) {
                    ProgressBar.setVisible(true);
                }

                switch (p) {

                    case 100:
                        done();
                        break;

                    case INDETERMINATE:
                        ProgressBar.setValue(0);
                        ProgressBar.setStringPainted(false);
                        ProgressBar.setIndeterminate(true);
                        break;

                    default:
                        ProgressBar.setStringPainted(true);
                        ProgressBar.setIndeterminate(false);
                        ProgressBar.setValue(p);
                        break;
                }
            }
        });
    }

    /**
     * Defines the range
     *
     * @param min The minimum value of the progress range.
     * @param max The maximum value of the progress range.
     * @param rangeKnown If flase then min and max is ignored and a running
     * indicator is shown.
     */
    @Override
    public void startTask(int min, int max, boolean rangeKnown, boolean cancellable) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (rangeKnown == false) {
                    ProgressBar.setIndeterminate(true);
                    ProgressBar.setStringPainted(false);
                } else {
                    ProgressBar.setIndeterminate(false);
                    ProgressBar.setStringPainted(true);
                    ProgressBar.setMinimum(min);
                    ProgressBar.setMaximum(max);
                    ProgressBar.setValue(min);
                }
                cancelWasPressed = false;
            }
        });
    }

    /**
     * Indicates whether the cancel button was pressed since task started.
     */
    @Override
    public boolean isCancelled() {
        return cancelWasPressed;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        QueuePanel = new javax.swing.JPanel();
        QueueCommandLabel = new javax.swing.JLabel();
        QueueProgress = new javax.swing.JProgressBar();
        ProgressPanel = new javax.swing.JPanel();
        MessageLabel = new javax.swing.JLabel();
        ProgressBar = new javax.swing.JProgressBar();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        QueuePanel.setOpaque(false);
        QueuePanel.setPreferredSize(new java.awt.Dimension(247, 24));
        QueuePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/General"); // NOI18N
        QueueCommandLabel.setText(bundle.getString("StatusBarAsynchCommandQueueLabel")); // NOI18N
        QueueCommandLabel.setToolTipText(bundle.getString("QueueLabel_TT")); // NOI18N
        QueuePanel.add(QueueCommandLabel);

        QueueProgress.setMaximum(12);
        QueueProgress.setToolTipText(bundle.getString("MainPanelAsynchCommandsWaitingIndicator_TT")); // NOI18N
        QueueProgress.setString("0");
        QueueProgress.setStringPainted(true);
        QueueProgress.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                QueueProgressMouseClicked(evt);
            }
        });
        QueuePanel.add(QueueProgress);

        add(QueuePanel);

        ProgressPanel.setOpaque(false);
        ProgressPanel.setPreferredSize(new java.awt.Dimension(340, 24));

        MessageLabel.setText("test progress message");
        ProgressPanel.add(MessageLabel);

        ProgressBar.setStringPainted(true);
        ProgressBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ProgressBarMouseClicked(evt);
            }
        });
        ProgressPanel.add(ProgressBar);

        add(ProgressPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void ProgressBarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ProgressBarMouseClicked
        cancelWasPressed = true;
    }//GEN-LAST:event_ProgressBarMouseClicked

    private void QueueProgressMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_QueueProgressMouseClicked
        CommandRecorder cr = CommandRecorder.getInstance();
        // initialize command editor the first time it is requested
        if (commandEditor == null) {
            commandEditor = new CommandEditorImpl();
            cr.setCommandEditor(commandEditor);
        }
        JDialog ced = new JDialog();
        ced.setModal(true);
        ced.setLayout(new BorderLayout());
        ced.add(commandEditor, BorderLayout.CENTER);
        ced.pack();
        ced.setLocationRelativeTo(MindlinerMain.getInstance());
        ced.setVisible(true);
    }//GEN-LAST:event_QueueProgressMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel MessageLabel;
    private javax.swing.JProgressBar ProgressBar;
    private javax.swing.JPanel ProgressPanel;
    private javax.swing.JLabel QueueCommandLabel;
    private javax.swing.JPanel QueuePanel;
    private javax.swing.JProgressBar QueueProgress;
    // End of variables declaration//GEN-END:variables

    class QueueIndicatorUpdater extends TimerTask {

        @Override
        public void run() {
            CommandRecorder cr = CommandRecorder.getInstance();
            // the following is asking isExecutionModeAsynch without adding that function to OnlineManager
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (!OnlineManager.waitForServerMessages()) {
                        if (!QueueProgress.isVisible()) {
                            QueueProgress.setVisible(true);
                        }
                        if (!QueueCommandLabel.isVisible()) {
                            QueueCommandLabel.setVisible(true);
                        }
                        int qSize = cr.getQueue().size();
                        // push maximum out with the queue
                        if (QueueProgress.getMaximum() < qSize) {
                            QueueProgress.setMaximum(qSize);
                        }
                        QueueProgress.setValue(qSize);
                        QueueProgress.setString(Integer.toString(qSize));
                    } else {
                        if (QueueProgress.isVisible()) {
                            QueueProgress.setVisible(false);
                        }
                        if (QueueCommandLabel.isVisible()) {
                            QueueCommandLabel.setVisible(false);
                        }
                    }
                }
            });
        }
    }
}
