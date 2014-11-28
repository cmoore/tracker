// -*- Mode: jde; eval: (hs-hide-level 2) -*-

package io.ivy.tracker;

import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.concurrent.Callable;

import net.canarymod.commandsys.CommandListener;
import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.factory.ItemFactory;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.inventory.ItemType;

import net.canarymod.hook.HookHandler;
import net.canarymod.hook.entity.*;
import net.canarymod.hook.player.*;

import net.canarymod.plugin.PluginListener;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.api.nbt.*;
import net.canarymod.api.world.blocks.TileEntity;
import net.canarymod.api.world.Chunk;
import net.canarymod.api.world.position.Position;
import net.canarymod.hook.entity.DamageHook;
import net.canarymod.api.entity.Entity;
import net.canarymod.hook.entity.EntityDeathHook;
import net.canarymod.api.entity.living.humanoid.NonPlayableCharacter;
import net.canarymod.api.world.position.Location;
import net.canarymod.hook.system.ServerTickHook;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandListener;
import net.canarymod.chat.MessageReceiver;

import io.ivy.tracker.TrackerAi;
import net.canarymod.api.inventory.Inventory;
import net.canarymod.api.inventory.InventoryType;
import net.canarymod.api.entity.living.humanoid.npc.NPCBehaviorRegistry;
import net.canarymod.api.world.blocks.properties.BlockProperty;
import net.canarymod.api.world.blocks.Chest;
import net.canarymod.api.factory.NBTFactory;

public class TrackerListener implements PluginListener, CommandListener {

    private long timer = 0;
    private Hashtable<String,NonPlayableCharacter> npcs = new Hashtable<String,NonPlayableCharacter>();

    
    @HookHandler
    public void onInventoryHook(InventoryHook hook) {
        if (hook.getInventory().getInventoryType().equals(InventoryType.CHEST) && hook.isClosing()) {

            TileEntity x = (TileEntity) hook.getInventory();
            Block block_above = x.getBlock().getRelative(0, -1, 0);
            
            if (block_above.getType().equals(BlockType.SignPost)) {
                Sign the_sign = (Sign) block_above.getTileEntity();

                hook.getPlayer().notice("It's a fucking sign up there, y0!");
                
                if (the_sign.getTextOnLine(0).equals("Iron Ingots")) {
                    // handle iron ingots.
                }
            }
        }
    }
    
    @HookHandler
    public void onServerTickHook(ServerTickHook hook) {
        long now = System.currentTimeMillis() / 1000;
        if ( now > (timer + 5)) {
            timer = now;
            periodic_handler();
        }
    }
    
    @SuppressWarnings("deprecation")
    @HookHandler
    public void onBlockRightClickHook(BlockRightClickHook hook) {
        
        Player player = hook.getPlayer();
        Block clicked = hook.getBlockClicked();
        
        if ((clicked.getType().equals(BlockType.SignPost)) && player.isAdmin()) {
            Sign sign = (Sign) clicked.getTileEntity();

            if (sign.getTextOnLine(0).equals("view")) {
                Block below_block = clicked.getRelative(0, -1, 0);
                if (below_block.getType().equals(BlockType.Chest)) {
                    player.notice("Yep, it's a chest.");
                    below_block
                        .getTileEntity()
                        .getMetaTag()
                        .keySet().stream().map(key -> (Callable<String>) () -> {
                                player.notice("M: ".concat(key));
                                return key;
                            });
                }
            }

            if (sign.getTextOnLine(0).equals("sticky") && player.isAdmin()) {
                
                player.getInventory().clearContents();
                player.getInventory().update();
                
                ItemFactory factory = Canary.factory().getItemFactory();
                
                Item stick = factory.newItem(ItemType.Stick);
                stick.setDisplayName("Stick of Ownership");
                stick.setMaxAmount(1);
                stick.setLore("Make it your own.");
                stick.getMetaTag().put("ivy.type", "ownership");
                player.getInventory().addItem(stick);
                player.getInventory().update();

                
                Item reset = factory.newItem(ItemType.Stick);
                reset.setDisplayName("Stick of Reset");
                reset.getMetaTag().put("ivy.type", "reset");
                reset.setMaxAmount(1);
                reset.setLore("Removes Ownership");
                player.getInventory().addItem(reset);
                player.getInventory().update();

                
                Item view = factory.newItem(ItemType.Stick);
                view.setDisplayName("Stick of Viewing");
                view.getMetaTag().put("ivy.type","viewing");
                view.setMaxAmount(1);
                view.setLore("View Tags");
                player.getInventory().addItem(view);
                player.getInventory().update();
                

                Item vendor = factory.newItem(ItemType.Stick);
                vendor.setDisplayName("Stick of Vendor");
                vendor.getMetaTag().put("ivy.type","vendor");
                vendor.setMaxAmount(1);
                vendor.setLore("Marks a chest as a vendor chest.");
                player.getInventory().addItem(vendor);
                player.getInventory().update();
                
            }
            
            if (sign.getTextOnLine(0).equals("NPC")) {
                if (!npcs.containsKey("hank")) {
                    Canary.log.info("NPC time...");
                } else {
                    Canary.log.info("Hank's already here, dude.");
                }
            }
        }
    }

    @HookHandler
    public void onBlockLeftClickHook(BlockLeftClickHook hook) {
        Player player = hook.getPlayer();
        Block clicked = hook.getBlock();
        
        if (player.isAdmin() && player.getItemHeld() != null) {
            Item wot = player.getItemHeld();
            
            if (wot.getType().equals(ItemType.Stick)) {
                String stick_type = wot.getMetaTag().getString("ivy.type");
                
                if (clicked.getTileEntity().getMetaTag() != null) {
                    CompoundTag tag = clicked.getTileEntity().getMetaTag();
                    
                    if (stick_type != null && tag != null) {
                        if (stick_type.equals("ownership")) {
                            apply_ownership(clicked, player);
                        }
                        if (stick_type.equals("reset")) {
                            tag.remove("ivy.type");
                        }
                        if (stick_type.equals("viewing")) {
                            tag.keySet().stream().forEach((k) ->
                                                          player.notice("K: " + k + " V: " + tag.get(k)));
                        }

                        /*
                          
                          ivy.vendor -> "all" | 

                         */
                        if (stick_type.equals("vendor")) {
                            if (clicked.getType().equals(BlockType.Chest)) {
                                tag.put("ivy.vendor", "all");
                            }
                        }
                    }
                }
            }
        }
    }
    
    @HookHandler
    public void onBlockDestroyHook(BlockDestroyHook hook) {
    }
	
    @HookHandler
    public void onLogin(ConnectionHook hook) {
    }





    /*


      Mob and Combat handlers.


    */


    
    @HookHandler
    public void onEntitySpawnHook( EntitySpawnHook hook ) {
    }
    
    @HookHandler
    public void onPlayerDeathHook( PlayerDeathHook hook ) {
    }
    

    @HookHandler
    public void onEntityDeathHook( EntityDeathHook hook ) {
    }
    

    @HookHandler
    public void onDamageHook(DamageHook hook) {
    }




    /*


      Utils


    */


    private void periodic_handler() {
        // npcs.entrySet().stream().forEach((npc) ->
        //                                  npc.getValue().chat("Honk!"));
    }

    public void setup() {
        // Location new_loc = new Location( 509.0, 71.0, 671.0 );
        
        // NonPlayableCharacter hank =
        //     Canary
        //     .factory()
        //     .getEntityFactory()
        //     .newNPC("Hank", new_loc);
        // // //hank.getAITaskManager().addTask(10, NPCAi.class);
        // // //hank.setDisplayName("Hank");
        // // hank.setShowDisplayName(true);
        // // hank.spawn();
        // // npcs.put("hank",hank);
        // NPCAi x = new NPCAi(hank);
        
        // NPCBehaviorRegistry.registerNPCListener(x, hank, true);
    }
    
    public void stop() {
        // npcs.entrySet().stream().forEach((entry) ->
        //                                  entry.getValue().despawn());
    }
    


    private List<Sign> find_signs(World world) {
        List<Sign> signs = null;
        
        world.getLoadedChunks().forEach(new Consumer<Chunk>() {
                @Override
                public void accept(Chunk chunk) {
                    if (chunk.getTileEntityMap().size() > 0) {
                        chunk.getTileEntityMap().forEach(new BiConsumer<Position,TileEntity>() {
                                @Override
                                public void accept(Position position, TileEntity tileentity) {
                                    if (tileentity.getBlock().getType().equals(BlockType.SignPost)) {
                                        Sign sign = (Sign) tileentity;
                                        signs.add(sign);
                                    }
                                }
                            });
                    }
                }
            });
        return signs;
    }

    public void apply_ownership(Block block, Player player) {
        CompoundTag tag = block.getTileEntity().getMetaTag();
        if (tag != null) {
            if ((tag.getString("ivy.owner") != null) &&
                (tag.getString("ivy.owner").length() > 1)) {
                if (tag.getString("ivy.owner").equals(player.getName())) {

                    // Already own it.
                    return;
                } else {
                    player.message("That block is owned by " + tag.getString("ivy.owner") + ".");
                }
            } else {
                tag.put("ivy.owner", player.getName());
            }
        }
    }

    public String is_owned(Block block) {
        CompoundTag tag = block.getTileEntity().getMetaTag();
        return tag != null ? tag.getString("ivy.owner") : null;
    }

    private void show_tags(CompoundTag ctag, Player player) {
        ctag.keySet().forEach(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    player.notice("S: " + s + " V: " + ctag.get(s).toString());
                }});
    }
}
