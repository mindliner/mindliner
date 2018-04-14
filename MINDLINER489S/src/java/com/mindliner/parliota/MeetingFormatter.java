/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.parliota;

import com.mindliner.parliota.objects.ParAgendaItem;
import com.mindliner.parliota.objects.ParAgendaItemComparator;
import com.mindliner.parliota.objects.ParMeeting;
import java.util.Collections;

/**
 * Takes a meeting and prints it out onto the console.
 * @author Marius Messerli (marius@mindlner.com)
 */
public class MeetingFormatter {

    public static void formatMeeting(ParMeeting meeting) {
        System.out.println(meeting.toString());
        System.out.println("Agenda items:");
        Collections.sort(meeting.getAgendaItems(), new ParAgendaItemComparator().reversed());
        int agendaItemCounter = 1;
        for (ParAgendaItem ai : meeting.getAgendaItems()) {
            System.out.println("\t" + Integer.toString(agendaItemCounter++) + ": " + ai.toString());
            ai.getComments().forEach((c) -> {
                System.out.println("\t\tC: " + c.getText() + " (by " + c.getAuthor().getName() + ")");
            });
        }
        System.out.println("\tParticipants:");
        meeting.getParticipants().values().forEach((p) -> {
            System.out.println("\t" + p.toString());
        });

    }

}
