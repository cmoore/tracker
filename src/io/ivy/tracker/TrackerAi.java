// -*- Mode: jde; eval: (hs-hide-level 2) -*-

package io.ivy.tracker;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import net.canarymod.Canary;
import net.canarymod.api.entity.Entity;
import net.canarymod.api.ai.AIBase;
import net.canarymod.api.entity.living.humanoid.NonPlayableCharacter;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;

public abstract class TrackerAi implements AIBase {
    private NonPlayableCharacter npc = null;

    List<Block> flowers = new ArrayList<Block>();

    Block target = null;

    List<Short> good = Arrays.asList(BlockType.RedTulip.getId(),
                                     BlockType.Dandelion.getId());

    boolean spam = false;

    public void NPCAi(NonPlayableCharacter npc) {
        this.npc = npc;
    }

    public boolean shouldExecute() {
        if (this.target == null) {
            return this.getTarget();
        }
        return false;
    }

    public boolean continueExecuting() {
        return this.target != null;
    }

    public boolean isContinuous() {
        return true;
    }

    public void startExecuting() {
        npc.getPathFinder().setSpeed(.4F);
        npc.getPathFinder().setPathToBlock(target);
    }

    public void resetTask() {
        target = null;
    }

    public void updateTask() {
        if (target == null) {
            return;
        }

        if (Math.abs(target.getX() - npc.getX()) < 1 &&
            Math.abs(target.getZ() - npc.getZ()) < 1) {
            npc.chat("I found a beautiful fucking flower.");
            flowers.add(target);
            this.resetTask();
        }
    }

    private boolean getTarget() {
        Block block = null;
        for (double x =npc.getX() - 20 ; x < npc.getX() + 20; x++) {
            for (double y =npc.getY() - 20 ; y < npc.getY() + 20; y++) {
                for (double z =npc.getZ() - 20 ; z < npc.getZ() + 20; z++) {
                    block = npc.getWorld().getBlockAt((int)x, (int)y, (int)z);
                    if (good.contains(block.getTypeId()) && !flowers.contains(block)) {
                        target = block;
                        spam = false;
                        return true;
                    }
                }
            }
        }
        if (!spam) {
            npc.chat("I can't find any more flowers. *sniff*");
            spam = true;
        }
        return false;
    }
}
