package com.xl.interfaces;

/**
 * The listener interface for receiving options events.
 * The class that is interested in processing an options
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addOptionsListener<code> method. When
 * the options event occurs, that object's appropriate
 * method is invoked.
 */
public interface OptionsListener {

    /**
     * Options changed.
     */
    public void optionsChanged();
}