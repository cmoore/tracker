// -*- Mode: jde; eval: (hs-hide-level 2) -*-

package io.ivy.tracker;

import net.canarymod.Canary;
import net.canarymod.plugin.Plugin;

public class Tracker extends Plugin {
	
    @Override
    public boolean enable() {
        Canary.hooks().registerListener(new TrackerListener(), this);
        return true;
    }

    @Override
    public void disable() {
    }
}
