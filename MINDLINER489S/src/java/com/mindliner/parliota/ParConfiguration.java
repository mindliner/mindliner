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

/**
 *
 * @author Marius Messerli (marius@mindliner.com)
 */
public class ParConfiguration {

    // full node
    public static final String HOSTNAME = "mindlineriota.ddns.net";
    public static final String PORT = "14265";

    // IOTA network configs
    public static final int DEPTH = 4;
    public static final int MINWEIGHTMAGNITUDE = 14;
    public static final int BALANCES_CONFIRMATION_THRESHOLD = 100;

    /**
     * We are following the standard here by choosing 2
     *
     * Security Level 1: 81-trit security Security Level 2: 162-trit security
     * Security Level 3: 243-trit security
     */
    public static final int ADDR_SECURITY_LEVEL = 2;

    // Parliota tags
    public static final String MEETING_CREATION_TAG = "TP_M_CREATED";
    public static final String AGENDA_ITEM_CREATION_TAG = "TP_A_CREATED";
    public static final String AGENDA_ITEM_PERSISTANCE_TAG = "TP_A_PERSIST";
    public static final String AGENDA_ITEM_APPROVE_TAG = "TP_A_APPROVE";
    public static final String PARTICIPANT_ADDED_TAG = "TP_P_ADDED";
    public static final String COMMENT_ADDED_TAG = "TP_C_ADDED";

}
