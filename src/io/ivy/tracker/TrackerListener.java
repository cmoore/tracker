// -*- Mode: jde; eval: (hs-hide-level 2) -*-

package io.ivy.tracker;

import java.util.List;
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
import net.canarymod.hook.entity.EntitySpawnHook;
import net.canarymod.hook.player.BlockDestroyHook;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.BlockLeftClickHook;
import net.canarymod.hook.player.ConnectionHook;
import net.canarymod.plugin.PluginListener;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.api.nbt.*;
import net.canarymod.api.world.blocks.TileEntity;
import net.canarymod.api.world.Chunk;
import net.canarymod.api.world.position.Position;

public class TrackerListener implements PluginListener, CommandListener {
		
    private void show_tags(CompoundTag ctag, Player player) {
        ctag.keySet().forEach(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    player.notice("S: " + s + " V: " + ctag.get(s).toString());
                }});
        
    }
	
    @SuppressWarnings("deprecation")
    @HookHandler
    public void onBlockRightClickHook(BlockRightClickHook hook) {
        
        Player player = hook.getPlayer();
        Block the_block = hook.getBlockClicked();

        if (the_block.getType().equals(BlockType.SignPost)) {
            Sign sign = (Sign) the_block.getTileEntity();

            if (sign.getTextOnLine(0).equals("lock")) {
                Block below_block = the_block.getRelative(0, -1, 0);
                if (below_block.getType().equals(BlockType.Chest)) {
                    player.notice("It's a chest.");
                    below_block.getTileEntity().getMetaTag().put("owner",player.getName());
                }
            }
            
            if (sign.getTextOnLine(0).equals("view")) {
                Block below_block = the_block.getRelative(0, -1, 0);
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

            if (sign.getTextOnLine(0).equals("reset inventory")) {
                player.getInventory().clearContents();
            }

            if (sign.getTextOnLine(0).equals("sticky")) {
                player.getInventory().clearContents();
                
                ItemFactory factory = Canary.factory().getItemFactory();
                
                Item stick = factory.newItem(ItemType.Stick);
                stick.setDisplayName("Stick of Ownership");
                stick.setMaxAmount(1);
                stick.setLore("Make it your own.");
                stick.getMetaTag().put("type", "ownership");
                player.getInventory().addItem(stick);
                player.getInventory().update();

                Item reset = factory.newItem(ItemType.Stick);
                reset.setDisplayName("Stick of Reset");
                reset.getMetaTag().put("type", "reset");
                reset.setMaxAmount(1);
                reset.setLore("Removes Ownership");
                player.getInventory().addItem(reset);
                player.getInventory().update();
                
                Item view = factory.newItem(ItemType.Stick);
                view.setDisplayName("Stick of Viewing");
                view.getMetaTag().put("type","viewing");
                view.setMaxAmount(1);
                view.setLore("View Tags");
                player.getInventory().addItem(view);
                player.getInventory().update();
            }
            
            if (sign.getTextOnLine(0).equals("spawn")) {
                String to_player = sign.getTextOnLine(1);
                if (player.getName().equals(to_player)) {
                    player.setSpawnPosition(sign.getBlock().getLocation());
                    player.notice("Spawn Set");
                }
            }
        }
    }

    @HookHandler
    public void onBlockLeftClickHook(BlockLeftClickHook hook) {
        Player player = hook.getPlayer();
        
    }
    
    @HookHandler
    public void onEntitySpawnHook(EntitySpawnHook hook) {
		}
    
	
    @HookHandler
    public void onBlockDestroyHook(BlockDestroyHook hook) {
    }
	
    @HookHandler
    public void onLogin(ConnectionHook hook) {
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
                
}
