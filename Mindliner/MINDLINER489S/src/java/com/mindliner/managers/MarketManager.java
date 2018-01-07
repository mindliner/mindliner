/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.SoftwareFeature;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * This class performs transactions with respect to the MindMarket.
 *
 * 9-DEC-2015
 *
 * @author Marius Messerli
 */
@Stateless
public class MarketManager implements MarketManagerRemote, MarketManagerLocal {

    @PersistenceContext
    private EntityManager em;


    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
}
