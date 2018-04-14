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
package com.mindliner.parliota;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mindliner.parliota.objects.ParAgendaItem;
import com.mindliner.parliota.objects.ParComment;
import com.mindliner.parliota.objects.ParMeeting;
import com.mindliner.parliota.objects.ParParticipant;
import com.mindliner.parliota.objects.ParObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jota.dto.response.GetTransferResponse;
import jota.error.ArgumentException;
import jota.model.Bundle;
import jota.model.Transaction;

/**
 *
 * @author Marius Messerli (marius@mindliner.com)
 */
public class TangleReader {

    private static final String TITLE_KEY = "title";
    private static final String DESCRIPTION_KEY = "description";
    private static final String START_KEY = "startDate";
    private static final String END_KEY = "endDate";

    private static final String AGENDA_ITEM_ADDRESS_KEY = "address";
    private static final String ITEM_ID_KEY = "itemId";

    private static final String PARTICIPANT_NAME_KEY = "name";

    private static final String COMMENT_TEXT_KEY = "text";
    private static final String COMMENT_AUTHOR_ADDRESS_KEY = "authorAddress";

    private final WalletService ws = WalletService.getInstance();

    /**
     * Loads and parses the transactions of the session seed to determine the
     * meeting structure.
     *
     * @param seed The IOTA seed for the meeting
     * @return The meeting or null if there was no seed specified.
     * @throws DataStructureException Indicates that the meeting creation tag
     * was not found
     */
    public ParMeeting loadMeeting(String seed) throws DataStructureException {
        try {
            if (!jota.utils.InputValidator.isHash(seed)) {
                throw new ArgumentException("invalid value for seed");
            }

            System.out.print("Loading meeting data ...");
            GetTransferResponse transferResponse = ws.getTransfers(seed, 30);
            Bundle[] transfers = transferResponse.getTransfers();

            int transferIndex = 0;

            /**
             * The meeting header is in the first transaction of the first
             * transfer. And since this is a zero value transaction we don't
             * need to update balances.
             */
            Transaction tx = transfers[transferIndex++].getTransactions().get(0);
            String tag = ws.decodeTag(tx.getTag());
            if (!ParConfiguration.MEETING_CREATION_TAG.equals(tag)) {
                throw new DataStructureException("Meeting creation tag not found where expected");
            }
            String msg = ws.decodeMessage(tx.getSignatureFragments());
            ParMeeting meeting = parseMeeting(msg);
            meeting.setSeed(seed);

            List<ParliotaTransaction> partxs = parseTransactions(transferIndex, transfers);
            loadAgendaItems(meeting, partxs);
            loadParticipants(meeting, partxs);
            loadComments(meeting, partxs);
            System.out.println("done\n");
            return meeting;
        } catch (ArgumentException ex) {
            Logger.getLogger(TangleReader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void loadAgendaItems(ParMeeting m, List<ParliotaTransaction> parliotaTransactions) {
        Map<Integer, String> addressMap = createItemIdAddressMap(parliotaTransactions);
        List<ParliotaTransaction> agendaItemCreationTransactions = getAgendaItemCreationTransactions(parliotaTransactions);
        for (ParliotaTransaction ptx : agendaItemCreationTransactions) {
            ParObject newObject = parseObject(ptx.getMessage());
            int itemId = parseItemId(ptx.getMessage());
            ParAgendaItem ai = new ParAgendaItem(newObject.getHeadline(), newObject.getDescription(), itemId);
            String address = addressMap.get(itemId);
            if (address != null) {
                ai.setAddress(address);
                ai.setApproved(true);

            } else {
                ai.setApproved(false);
            }
            m.getAgendaItems().add(ai);
        }
    }

    private void loadParticipants(ParMeeting m, List<ParliotaTransaction> parliotaTransactions) {
        parliotaTransactions.stream()
                .filter((ptx) -> (ParConfiguration.PARTICIPANT_ADDED_TAG.equals(ptx.getTag())))
                .forEachOrdered((ptx) -> {
                    String name = parseParticipantName(ptx.getMessage());
                    String address = parseAgendaItemAddress(ptx.getMessage());
                    m.addParticipant(address, name);
                });
    }

    private void loadComments(ParMeeting m, List<ParliotaTransaction> parliotaTransactions) {
        parliotaTransactions.stream()
                .filter((ptx) -> (ParConfiguration.COMMENT_ADDED_TAG.equals(ptx.getTag())))
                .forEachOrdered((ptx) -> {
                    String authorAddress = parseCommentAuthorAddress(ptx.getMessage());
                    String commentText = parseCommentText(ptx.getMessage());
                    ParAgendaItem ai = m.getAgendaItem(ptx.getAddress());
                    ParParticipant p = m.getParticipant(authorAddress);
                    if (ai != null && p != null) {
                        ParComment c = new ParComment(ai, p, commentText);
                        ai.getComments().add(c);
                    }
                });

    }

    private Map<Integer, String> createItemIdAddressMap(List<ParliotaTransaction> parliotaTransactions) {
        Map<Integer, String> result = new HashMap<>();
        parliotaTransactions.stream()
                .filter((ptx) -> (ParConfiguration.AGENDA_ITEM_APPROVE_TAG.equals(ptx.getTag())))
                .forEachOrdered((ptx) -> {
                    int itemId = parseItemId(ptx.getMessage());
                    String address = parseAgendaItemAddress(ptx.getMessage());
                    result.put(itemId, address);
                });
        return result;
    }

    private List<ParliotaTransaction> getAgendaItemCreationTransactions(List<ParliotaTransaction> allTx) {
        List<ParliotaTransaction> result = new ArrayList<>();
        allTx.stream().filter((ptx) -> (ParConfiguration.AGENDA_ITEM_CREATION_TAG.equals(ptx.getTag()))).forEachOrdered((ptx) -> {
            result.add(ptx);
        });
        return result;
    }

    private List<ParliotaTransaction> parseTransactions(int startIndex, Bundle[] transfers) {
        List<ParliotaTransaction> result = new ArrayList<>();
        for (int idx = startIndex; idx < transfers.length; idx++) {
            Transaction tx = transfers[idx].getTransactions().get(0);
            ParliotaTransaction ptx = new ParliotaTransaction(
                    idx, tx.getPersistence(), tx.getAddress(),
                    tx.getValue(), ws.decodeTag(tx.getTag()), ws.decodeMessage(tx.getSignatureFragments()));
            result.add(ptx);
        }
        return result;
    }

    private ParObject parseObject(String jsonline) {
        JsonElement jelement = new JsonParser().parse(jsonline);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonPrimitive title = jobject.getAsJsonPrimitive(TITLE_KEY);
        JsonPrimitive description = jobject.getAsJsonPrimitive(DESCRIPTION_KEY);
        ParObject newObject = new ParObject(title.getAsString(), description.getAsString());
        return newObject;
    }

    private ParMeeting parseMeeting(String jsonline) {
        JsonElement jelement = new JsonParser().parse(jsonline);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonPrimitive title = jobject.getAsJsonPrimitive(TITLE_KEY);
        JsonPrimitive description = jobject.getAsJsonPrimitive(DESCRIPTION_KEY);
        ParMeeting m = new ParMeeting(title.getAsString(), description.getAsString());
        JsonPrimitive startDate = jobject.getAsJsonPrimitive(START_KEY);
        JsonPrimitive endDate = jobject.getAsJsonPrimitive(END_KEY);
        if (startDate != null && endDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                m.setStart(sdf.parse(startDate.getAsString()));
                m.setEnd(sdf.parse(endDate.getAsString()));
            } catch (ParseException ex) {
                Logger.getLogger(TangleReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return m;
    }

    private int parseItemId(String jsonline) {
        JsonElement jelement = new JsonParser().parse(jsonline);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonPrimitive jp = jobject.getAsJsonPrimitive(ITEM_ID_KEY);
        return jp.getAsInt();
    }

    private String parseParticipantName(String jsonline) {
        JsonElement jelement = new JsonParser().parse(jsonline);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonPrimitive jp = jobject.getAsJsonPrimitive(PARTICIPANT_NAME_KEY);
        return jp.getAsString();
    }

    private String parseAgendaItemAddress(String jsonline) {
        JsonElement jelement = new JsonParser().parse(jsonline);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonPrimitive jp = jobject.getAsJsonPrimitive(AGENDA_ITEM_ADDRESS_KEY);
        return jp.getAsString();
    }

    private String parseCommentAuthorAddress(String jsonline) {
        JsonElement jelement = new JsonParser().parse(jsonline);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonPrimitive jp = jobject.getAsJsonPrimitive(COMMENT_AUTHOR_ADDRESS_KEY);
        return jp.getAsString();
    }

    private String parseCommentText(String jsonline) {
        JsonElement jelement = new JsonParser().parse(jsonline);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonPrimitive jp = jobject.getAsJsonPrimitive(COMMENT_TEXT_KEY);
        return jp.getAsString();
    }

    class ParliotaTransaction {

        int index;
        boolean confirmed;
        String address;
        long value;
        String tag;
        String message;

        public ParliotaTransaction(int index, boolean confirmed, String address, long value, String tag, String message) {
            this.index = index;
            this.confirmed = confirmed;
            this.address = address;
            this.value = value;
            this.tag = tag;
            this.message = message;
        }

        public int getIndex() {
            return index;
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public String getAddress() {
            return address;
        }

        public long getValue() {
            return value;
        }

        public String getTag() {
            return tag;
        }

        public String getMessage() {
            return message;
        }

    }

}
