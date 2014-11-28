// -*- Mode: jde; eval: (hs-hide-level 2) -*-

package io.ivy.tracker;

import net.canarymod.Canary;
import net.canarymod.plugin.Plugin;

public class Tracker extends Plugin {

    private TrackerListener listener;
    
    @Override
    public boolean enable() {
        listener = new TrackerListener();
        listener.setup();
        
        Canary.hooks().registerListener(listener, this);
        return true;
    }

    @Override
    public void disable() {
        listener.stop();
    }
}
