/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.prefs;

/**
 * This interface is implemented by classes that need to store and restore some
 * settings between sessions. The corresponding load preferences function is
 * assumed to have private access in the class that implements this interface.
 *
 * @author Marius Messerli
 */
public interface MlPreference {

    public void storePreferences();

}
