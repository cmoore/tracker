package io.ivy.tracker;


import net.canarymod.Canary;
import net.canarymod.api.entity.Entity;
import net.canarymod.api.entity.EntityType;
import net.canarymod.api.entity.living.animal.Horse;
import net.canarymod.api.entity.living.humanoid.NonPlayableCharacter;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.entity.living.monster.Zombie;
import net.canarymod.api.factory.EntityFactory;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.api.scoreboard.Score;
import net.canarymod.api.scoreboard.ScoreObjective;
import net.canarymod.api.scoreboard.ScorePosition;
import net.canarymod.api.scoreboard.Scoreboard;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.entity.EntitySpawnHook;
import net.canarymod.hook.player.BlockDestroyHook;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.ConnectionHook;
import net.canarymod.hook.player.LevelUpHook;
import net.canarymod.hook.world.TimeChangeHook;
import net.canarymod.plugin.PluginListener;
import net.canarymod.warp.Warp;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.api.world.position.Location;

public class TrackerListener implements PluginListener {

	public Integer get_level( Player player ) {
		Scoreboard scoreboard = Canary.scoreboards().getScoreboard( "level_scoreboard");
		ScoreObjective obj = scoreboard.getScoreObjective("level_objective");
		return scoreboard.getScore(player, obj).getScore();
	}

	public Integer get_experience( Player player ) {
		Scoreboard scoreboard = Canary.scoreboards().getScoreboard("experience_scoreboard");
		ScoreObjective sobjective = scoreboard.getScoreObjective("experience_objective");
		return scoreboard.getScore(player, sobjective).getScore();
	}

	public void add_experience(Player player, Integer amount) {
		Scoreboard scoreboard = Canary.scoreboards().getScoreboard("experience_scoreboard");
		ScoreObjective scoreboard_objective = scoreboard.getScoreObjective("experience_objective");
		Score xp = scoreboard.getScore(player, scoreboard_objective);
		xp.addToScore(amount);
		xp.update();
		update_level( player );
	}

	public void update_level(Player player) {
		Integer current_xp = get_experience( player );
		Integer old_level = get_level( player );
		
		Double current_level = 0.0;
		Integer current_level_i = 0;
		
		current_level = .05 * Math.sqrt(current_xp) + 1;
		
		if (current_level < 1) {
			current_level = 1.0;
		}
		
		current_level_i = current_level.intValue();
		
		if (old_level < current_level_i && current_level_i > 1) {
			player.notice("LEVEL UP!");
		}
		Scoreboard level = Canary.scoreboards().getScoreboard( "level_scoreboard");
		ScoreObjective level_o = level.getScoreObjective( "level_objective" );
		Score level_score = level.getScore(player, level_o);
		level_score.setScore(current_level.intValue());
		level_score.update();
	}

	
	@HookHandler
	public void onTimeChangeHook(TimeChangeHook hook) {
		
	}
	
	
	@SuppressWarnings("deprecation")
	@HookHandler
	public void onBlockRightClickHook(BlockRightClickHook hook) {
		Player player = hook.getPlayer();
		Block the_block = hook.getBlockClicked();

		if (the_block.getType() == BlockType.SignPost) {
			Sign sign = (Sign) hook.getBlockClicked().getTileEntity();

			if (sign.getTextOnLine(0).equals("testing")) {
				player.notice("Spawning...");
				EntityFactory the_fac = Canary.factory().getEntityFactory();
				NonPlayableCharacter npc = the_fac.newNPC("Gorgon", player.getLocation());
				npc.teleportTo(player.getLocation());
				if (npc.canSpawn()) {
					Canary.log.info("canSpawn is true.");
				}
				npc.spawn();
			}
			if (sign.getTextOnLine(0).equals("To Henry") &&
					sign.getTextOnLine(1).equals("World")) {
				Warp the_warp = Canary.warps().getWarp("henry");
				the_warp.warp(player);
			}
			
			if (sign.getTextOnLine(0).equals("Dover")) {
				player.teleportTo(1876, 84, 451);
			}
			
			if (sign.getTextOnLine(0).equals("Cave")) {
				player.teleportTo(1531, 81, 1225);
			}
			if (sign.getTextOnLine(0).equals("Diggy")) {
				player.teleportTo(2364, 70, 193);
			}
			
			if (sign.getTextOnLine(0).equals("return")) {
				player.teleportTo(player.getSpawnPosition());
				the_block.setType(BlockType.Air);
				the_block.update();
			}
			
			if (sign.getTextOnLine(0).equals("spawn")) {
				String to_player = sign.getTextOnLine(1);
				if (player.getName().equals(to_player)) {
					player.setSpawnPosition(sign.getBlock().getLocation());
					player.notice("Spawn Set");
				}
			}

			if (sign.getTextOnLine(0).equals("Coal")) {
				player.getInventory().addItem(ItemType.Coal, 20);
				player.getInventory().update();
			}
			if (sign.getTextOnLine(0).equals("Torches")) {
				player.getInventory().addItem(ItemType.Torch, 20);
				player.getInventory().update();
			}
			if (sign.getTextOnLine(0).equals("Chicken")) {
				player.getInventory().addItem(ItemType.CookedChicken, 5);
				player.getInventory().update();
			}
						
			if (sign.getTextOnLine(0).equals("Redstone")) {
				player.getInventory().addItem(ItemType.RedStone, 64);
				player.getInventory().update();
			}
			
			if (sign.getTextOnLine(0).equals("Repeater")) {
				player.getInventory().addItem(ItemType.RedstoneRepeater, 64);
				player.getInventory().addItem(ItemType.StickyPiston, 1);
				player.getInventory().addItem(ItemType.RedstoneTorchOn, 64);
				player.getInventory().update();
			}
			
			if (sign.getTextOnLine(0).equals("Wool")) {
				player.getInventory().addItem(ItemType.WoolRed);
				player.getInventory().update();
			}
			
			if (sign.getTextOnLine(0).equals("Picks")) {
				player.getInventory().addItem(ItemType.StonePickaxe, 10);
				player.getInventory().update();
			}
			if (sign.getTextOnLine(0).equals("Swords")) {
				player.getInventory().addItem(ItemType.StoneSword, 10);
				player.getInventory().update();
			}

			if (sign.getTextOnLine(0).equals("Armor")) {
				player.getInventory().addItem(ItemType.IronHelmet, 1);
				player.getInventory().addItem(ItemType.IronBoots, 1);
				player.getInventory().addItem(ItemType.IronChestplate, 1);
				player.getInventory().addItem(ItemType.IronLeggings, 1);
				player.getInventory().update();
			}

			
			
			if (sign.getTextOnLine(0).equals("100xp")) {
				add_experience(player, 100);
			}
			if (sign.getTextOnLine(0).equals("resetxp")) {
				Scoreboard scoreboard = Canary.scoreboards().getScoreboard("experience_scoreboard");
				ScoreObjective obj = scoreboard.getScoreObjective("experience_objective");
				Score level = scoreboard.getScore(player,  obj);
				level.setScore(0);
				level.update();
			}
		}
	}

	@HookHandler
	public void onEntitySpawnHook(EntitySpawnHook hook) {
		Entity entity = hook.getEntity();
		if (entity.getEntityType().equals(EntityType.NONPLAYABLECHARACTER)) {
			NonPlayableCharacter npc = (NonPlayableCharacter) hook.getEntity();
			Canary.log.info("NPC NAMED");
			Canary.log.info(npc.getDisplayName());
		}
		
/*		if (entity.getEntityType().equals(EntityType.ZOMBIE)) {
			Zombie zombie = (Zombie) entity;
			Horse horse = (Horse) Canary.factory().getEntityFactory().newEntity(EntityType.SKELETONHORSE, zombie.getLocation());
			horse.setTamed(true);
			horse.setOwner(zombie);
			horse.spawn();
			Canary.log.info("Yeee haw!");
		}
		*/
	}

	@HookHandler
	public void onLevelUpHook(LevelUpHook hook) {
		add_experience(hook.getPlayer(), 100);
	}
	
	@HookHandler
	public void onBlockDestroyHook(BlockDestroyHook hook) {
		Block the_block = hook.getBlock();
		if (the_block.getType() == BlockType.DiamondOre) {
			add_experience(hook.getPlayer(), 100);
		}
		if (the_block.getType() == BlockType.Stone) {
			add_experience(hook.getPlayer(), 1);
		}
		if (the_block.getType() == BlockType.Dirt | the_block.getType() == BlockType.Grass) {
			add_experience(hook.getPlayer(), 1);
		}
	}
	
	@HookHandler
	public void onLogin(ConnectionHook hook) {
		Player player = hook.getPlayer();
		
		Scoreboard level = Canary.scoreboards().getScoreboard("level_scoreboard");
		Scoreboard experience = Canary.scoreboards().getScoreboard("experience_scoreboard");
		
		ScoreObjective level_obj = level.addScoreObjective( "level_objective" );
		ScoreObjective experience_obj = experience.addScoreObjective( "experience_objective" );
		
		level_obj.setDisplayName( "Level" );
		experience_obj.setDisplayName( "Experience" );
		
		Score lvl_score = level.getScore( player, level_obj );
		Score xp_score = experience.getScore( player, experience_obj );
		
		// if they've never logged in, set their xp to 1.
		if (xp_score.getScore() < 1) {
			xp_score.setScore(0);
			xp_score.update();
		}

		Integer current_level = get_level(player);
		lvl_score.setScore(current_level);
		
		level.setScoreboardPosition( ScorePosition.PLAYER_LIST, level_obj );
		experience.setScoreboardPosition( ScorePosition.SIDEBAR, experience_obj );
	}
}

/*

Legendary Signs
 - Gives you 100 of something, 20 at a time.
 - Spread out in all sorts of places.
 
*/
