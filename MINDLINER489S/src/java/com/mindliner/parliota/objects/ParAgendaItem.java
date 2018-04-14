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
import java.util.ArrayList;
import java.util.List;

/**
 * The Parliota Agenda Item Model
 *
 * @author Marius Messerli <marius.messerli@mindliner.com>
 * 25-FEB-2018
 */

public class ParAgendaItem extends ParObject implements Serializable{

    private String address = "";
    private long value = 0;
    private long itemId = 0;
    private String authorName = "";
    private boolean approved = false;
    private List<ParComment> comments = new ArrayList<>();

    public ParAgendaItem(String headline, String description, long itemId) {
        super(headline, description);
        this.itemId = itemId;
    }

    public String getAddress() {
        return address;
    }

    public final void setAddress(String address) {
        this.address = address;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public List<ParComment> getComments() {
        return comments;
    }

    public void setComments(List<ParComment> comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        if (isApproved()) {
            return super.toString() + ", addr = " + getAddress().substring(0, 4) + "... value = " + getValue();
        } else {
            return "(tentative) : " + super.toString();
        }
    }

}
