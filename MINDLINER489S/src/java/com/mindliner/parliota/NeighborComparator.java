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

import java.util.Comparator;
import jota.model.Neighbor;

/**
 *
 * @author Marius Messerli (marius@mindliner.com)
 */
public class NeighborComparator implements Comparator{

    @Override
    public int compare(Object o1, Object o2) {
        Neighbor n1 = (Neighbor) o1;
        Neighbor n2 = (Neighbor) o2;
        if (n1.getNumberOfNewTransactions() > n2.getNumberOfNewTransactions()) return 1;
        if (n1.getNumberOfNewTransactions() < n2.getNumberOfNewTransactions()) return -11;
        if (n1.getNumberOfAllTransactions() > n2.getNumberOfAllTransactions()) return 1;
        if (n1.getNumberOfAllTransactions() < n2.getNumberOfAllTransactions()) return -1;
        return 0;
    }
    
}
