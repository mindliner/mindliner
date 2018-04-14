/*
 * Copyright 2018 marius.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mindliner.parliota.objects;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * The meeting object for Parliota.
 *
 * @author Marius Messerli <marius.messerli@mindliner.com>
 * 25-FEB-2018
 */
public class ParMeeting extends ParObject implements Serializable {

    private String seed;
    private Date start;
    private Date end;
    private List<ParAgendaItem> agendaItems = new ArrayList<>();
    private final Map<String, ParParticipant> participants = new HashMap<>();

    public ParMeeting(String headline, String description) {
        super(headline, description);
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public List<ParAgendaItem> getAgendaItems() {
        return agendaItems;
    }

    public void setAgendaItems(List<ParAgendaItem> agendaItems) {
        this.agendaItems = agendaItems;
    }

    public ParAgendaItem getAgendaItem(String address) {
        for (ParAgendaItem ai : agendaItems) {
            // cut off checksum
            if (ai.getAddress().substring(0, 81).equals(address)) {
                return ai;
            }
        }
        return null;
    }

    public void addParticipant(String address, String name) {
        if (participants.get(address) != null) {
            System.err.println("attempt to overwrite participant ignored - address: " + address);
        } else {
            participants.put(address, new ParParticipant(name, address));
        }
    }

    /**
     * Convenience function to get a participant by address
     *
     * @param address
     * @return
     */
    public ParParticipant getParticipant(String address) {
        return participants.get(address);
    }

    public Map<String, ParParticipant> getParticipants() {
        return participants;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        SimpleDateFormat sdf = new SimpleDateFormat();
        if (start != null) {
            sb.append(", start = ").append(sdf.format(start));
            if (end != null) {
                sb.append(", end = ").append(sdf.format(end));
            }
        }
        return sb.toString();
    }

}
