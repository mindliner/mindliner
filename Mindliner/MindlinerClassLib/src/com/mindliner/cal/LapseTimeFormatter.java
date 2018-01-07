/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.cal;

import java.text.NumberFormat;

/**
 *
 * @author marius
 */
public class LapseTimeFormatter {

    /**
     * Returns a string in the format "minutes : second"
     * @param duration The lapse duration in milliseconds
     * @return 
     */
    public static String formatMinutesAndSeconds(long duration) {
        NumberFormat secondsFormat = NumberFormat.getInstance();
        secondsFormat.setMinimumIntegerDigits(2);
        long minutes = duration / 1000 / 60;
        long seconds = (duration - minutes * 60 * 1000) / 1000;
        return minutes + ":" + secondsFormat.format(seconds);
    }

}
