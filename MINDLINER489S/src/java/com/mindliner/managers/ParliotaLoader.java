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
package com.mindliner.managers;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsObject;
import com.mindliner.parliota.ParConfiguration;
import com.mindliner.parliota.DataStructureException;
import com.mindliner.parliota.MeetingFormatter;
import com.mindliner.parliota.TangleReader;
import com.mindliner.parliota.WalletService;
import com.mindliner.parliota.objects.ParMeeting;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Marius Messerli (marius@mindliner.com)
 */
@Stateless
public class ParliotaLoader implements ParliotaLoaderLocal {

    @PersistenceContext(name = "ParliotaPersistenceContext")
    EntityManager em;

    @EJB
    private ObjectFactoryLocal objectFactory;

    

    /**
     * Loads the meeting with the specified seed from the IOTA tangle.
     *
     * @param seed The IOTA seed at which the meeting is stored
     * @param datapool
     * @param confidentiality
     * @param relative
     * @return The meeting object or null if loading failed.
     */
    @Override
    public ParMeeting load(String seed, mlsClient datapool, mlsConfidentiality confidentiality, mlsObject relative) {
        try {
            WalletService ws = WalletService.getInstance();
            jota.dto.response.GetNodeInfoResponse nodeInfo = ws.getNodeInfo();
            System.out.println("\nUsing node "
                    + ParConfiguration.HOSTNAME
                    + " [" + nodeInfo.getAppName() + " version " + nodeInfo.getAppVersion() + "]");
            System.out.println("Synched: "
                    + (nodeInfo.getLatestMilestoneIndex() == nodeInfo.getLatestSolidSubtangleMilestoneIndex()));
            if (!seed.substring(0, 2).equals("TP")) {
                System.err.println("\nThis is not a parliota seed. Seed needs to start with the letters 'TP'");
                System.exit(0);
            }
            TangleReader reader = new TangleReader();
            ParMeeting meeting = reader.loadMeeting(seed);
            MeetingFormatter.formatMeeting(meeting);

            return meeting;
        } catch (DataStructureException ex) {
            Logger.getLogger(ParliotaLoader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void persistMeeting(ParMeeting m, mlsClient datapool, mlsConfidentiality confidentiality, int relativeId) {

//         meeting = (mlsMeeting) objectFactory.createLocal(mlsMeeting.class, datapool, null,
//                confidentiality, relativeId, LinkRelativeType.OBJECT,
//                null, m.getHeadline(), m.getDescription());
//        em.persist(m);
//        em.flush();
//        System.out.println("meeting was stored with ID " + m.getId());
    }
    
}
