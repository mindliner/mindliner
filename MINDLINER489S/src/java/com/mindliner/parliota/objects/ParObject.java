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

/**
 *
 * @author Marius Messerli (marius@mindliner.com)
 */
public class ParObject implements Serializable{

    private String headline;
    private String description;

    public ParObject(String headline, String description) {
        this.headline = headline;
        this.description = description;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeadline()).append(": ").append(getDescription());        
        return sb.toString();
    }

}
