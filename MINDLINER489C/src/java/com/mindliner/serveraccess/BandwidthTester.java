/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.serveraccess;

import com.mindliner.managers.TestManagerRemote;
import java.util.Date;
import javax.naming.NamingException;

/**
 * This class performs some BW tests to get a quick feedback on what the
 * situation is.
 * 
 * @author Marius Messerli
 */
public class BandwidthTester {

    public static String testBandwidth() {
        try {
            StringBuilder sb = new StringBuilder();
            TestManagerRemote tmr = (TestManagerRemote) RemoteLookupAgent.getManagerForClass(TestManagerRemote.class);
            Date start;
            long duration = 0;

            // test upload speed
            int arraySize = 2048;
            sb.append("TESTING UPLOAD SPEED ---------------\n");
            while (duration < 2000 && arraySize < 2000000) {
                start = new Date();
                tmr.testUploadSpeed(new int[arraySize]);
                duration = (new Date()).getTime() - start.getTime();
                sb.append("upload of ").append(arraySize).append(" ints took ").append(duration).append(" millis").append("\n");
                arraySize *= 2;
            }

            // test download speed
            duration = 0;
            arraySize = 1024;
            sb.append("TESTING DOWNLOAD SPEED ---------------\n");
            while (duration < 2000 && arraySize < 2000000) {
                start = new Date();
                tmr.testDownloadSpeed(arraySize);
                duration = (new Date()).getTime() - start.getTime();
                sb.append("download of ").append(arraySize).append(" ints took ").append(duration).append(" millis").append("\n");
                arraySize *= 2;
            }

            // test connection
            start = new Date();
            int maxRepeats = 100;
            int pingCounts = 0;
            sb.append("TESTING CONNECTION SPEED ---------------\n");
            for (int i = 0; duration < 5000 && i < maxRepeats; i++) {
                tmr.testPing();
                pingCounts++;
                duration = (new Date()).getTime() - start.getTime();
            }
            duration = (new Date()).getTime() - start.getTime();
            sb.append("It took ").append(duration).append(" millis to connect the server for ").append(pingCounts).append(" times in a row").append("\n");
            return sb.toString();
        } catch (NamingException ex) {
            return ex.getMessage();

        }

    }
}
