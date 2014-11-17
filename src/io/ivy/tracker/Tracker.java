package io.ivy.tracker;

import net.canarymod.Canary;
import net.canarymod.plugin.Plugin;

public class Tracker extends Plugin {
	
    @Override
    public boolean enable() {
        getLogman().info("loading...");
        Canary.hooks().registerListener(new TrackerListener(), this);
        return true;
    }

    @Override
    public void disable() {
    }
}
