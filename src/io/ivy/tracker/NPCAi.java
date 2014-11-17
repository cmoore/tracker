package io.ivy.tracker;

import net.canarymod.Canary;
import net.canarymod.api.entity.Entity;
import net.canarymod.api.entity.living.humanoid.npc.NPCBehavior;
import net.canarymod.api.entity.living.humanoid.NonPlayableCharacter;
import net.canarymod.api.entity.living.humanoid.Player;

public abstract class NPCAi implements NPCBehavior {
    private NonPlayableCharacter npc;

    public NPCAi(NonPlayableCharacter npc) {
        this.npc = npc;
    }

    public void onAttack(Entity entity) {
    	Canary.log.info("NPC Attacked.");
    }

    public void onClicked(Player player) {
        if (npc != null) {
            npc.chat("Hello, " + player.getName());
        }
    }

    public void onUpdate() {
        if (npc != null) {
            npc.lookAtNearest();
        }
    }

    public void onDestroy() {
    	Canary.log.info("NPC Destroyed.");
    }
}
