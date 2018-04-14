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

import java.util.Comparator;

/**
 *
 * @author Marius Messerli (marius@mindliner.com)
 */
public class ParAgendaItemComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof ParAgendaItem && o2 instanceof ParAgendaItem)) {
            throw new IllegalArgumentException("Both arguments must be of the class AgendaItem");
        }
        ParAgendaItem a1 = (ParAgendaItem) o1;
        ParAgendaItem a2 = (ParAgendaItem) o2;
        if (a1.getValue() > a1.getValue()) {
            return 1;
        }
        if (a1.getValue() == a2.getValue()) {
            return 0;
        }
        return -1;

    }

}
