/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

/**
 * This enum lists all the object attributes available in Mindliner.
 *
 * @author Marius Messerli
 */
public enum ObjectAttributes {

    Id,
    Headline,
    Description,
    DataPool,
    Completion,
    ModificationDate,
    CreationDate,
    Confidentiality,
    Owner,
    Privacy,
    Archived,
    Rating,
    SynchState,
    ReviewStatus,
    Island,
    // task only fields
    TaskPriority,
    DueDate,
    // image only fields
    URL,
    // contact only fields
    Firstname,
    Middlename,
    Lastname,
    Email,
    Workphone,
    Mobilephone,
    WorkMinutes,
    RelativesOrdered // whether or not the relatives have a specific order
}
