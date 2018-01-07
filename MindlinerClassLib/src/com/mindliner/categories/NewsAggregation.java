/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.categories;

/**
 * Defines the aggregation mechanism for news
 *
 * @author Marius Messerli
 */
public class NewsAggregation {

    public static enum Grouping {
        None,
        ByEvent,
        ByActor,
        ByDay
    }

}
