/*
 *  QuestManager: An RPG plugin for the Bukkit API.
 *  Copyright (C) 2015-2016 Github Contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.skyisland.questmanager.player.skill.defaults;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;
import com.skyisland.questmanager.QuestManagerPlugin;
import com.skyisland.questmanager.configuration.utils.LocationState;
import com.skyisland.questmanager.configuration.utils.YamlWriter;
import com.skyisland.questmanager.player.QuestPlayer;
import com.skyisland.questmanager.player.skill.LogSkill;
import com.skyisland.questmanager.player.skill.QualityItem;
import com.skyisland.questmanager.player.skill.Skill;
import com.skyisland.questmanager.player.skill.event.FishEvent;
import com.skyisland.questmanager.region.Region;
import com.skyisland.questmanager.region.RegionManager;
import com.skyisland.questmanager.ui.menu.InventoryMenu;
import com.skyisland.questmanager.ui.menu.inventory.minigames.FishingGui;

public class FishingSkill extends LogSkill implements Listener {
	
	public static final String CONFIG_NAME = "Fishing.yml";
	
	public static final String BAD_RANGE_MESSAGE = ChatColor.RED + "There doesn't seem to be any fish in your skill range...";
	
	private static final class FishRecord {
		
		private int difficulty;
		
		private ItemStack icon;
		
		private Region region;
		
		private double weight;
		
		public FishRecord(int difficulty, double weight, ItemStack icon, Region region) {
			this.difficulty = difficulty;
			this.weight = weight;
			this.icon = icon;
			this.region = region;
		}
		
	}

	public Type getType() {
		return Skill.Type.TRADE;
	}
	
	public String getName() {
		return "Fishing";
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.FISHING_ROD);
	}
	
	public String getDescription(QuestPlayer player) {
		String ret = ChatColor.WHITE + "Fishing skill determines the types of fish caught and how difficulty it"
				+ " is to catch them.";
		
		int level = player.getSkillLevel(this);
		
		ret += "\n\n" + ChatColor.GOLD + "Fish Range: " 
				+ Math.max(0, level - maxDifficultyRange) + " - " + (level + maxDifficultyRange);
		ret += "\n" + ChatColor.GOLD + "Catch Quality: +" + (level * qualityRate);
		
		ret += "\n" + ChatColor.GREEN + "Difficulty: " 
				+ ((int) (100 - (100 * (level * reelDifficultyDiscount)))) + "%" + ChatColor.RESET;
		ret += "\n" + ChatColor.DARK_BLUE + "Reeling Time: "
				+ ((int) (100 - (100 * (level * timeDiscount))) + "%"); 
		
		return ret;
	}

	@Override
	public int getStartingLevel() {
		return startingLevel;
	}
	
	@Override
	public String getConfigKey() {
		return "Fishing";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof FishingSkill);
	}
	
	private int startingLevel;
	
	private double difficultyPerRow;
	
	private float baseReelDifficulty;
	
	private float baseReelDeviation;
	
	private float letRate;
	
	private float reelDifficultyRate;
	
	private double reelDifficultyDiscount;
	
	private double baseObstacleDifficulty;
	
	private double baseObstacleDeviation;
	
	private double obstacleDifficultyRate;
	
	private double obstacleDifficultyDiscount;
	
	private double baseTimePerDifficulty;
	
	private double timeDiscount;
	
	private double extraFishPerLevel;
	
	private int maxDifficultyRange;
	
	private double qualityRate;
	
	private boolean restrictLower;
	
	private List<FishRecord> fishRecords;
	
	public FishingSkill() {
		File configFile = new File(QuestManagerPlugin.questManagerPlugin.getDataFolder(),
				QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillPath() + CONFIG_NAME);
		YamlConfiguration config = createConfig(configFile);

		
		if (!config.getBoolean("enabled", true)) {
			return;
		}
		
		this.startingLevel = config.getInt("startingLevel", 0);
		this.difficultyPerRow = config.getDouble("difficultyPerRow", 15);
		this.baseReelDifficulty = (float) config.getDouble("baseReelDifficulty", .03);
		this.baseReelDeviation = (float) config.getDouble("baseReelDeviation", .02);
		this.letRate = (float) config.getDouble("letRate", 3.0);
		this.reelDifficultyRate = (float) config.getDouble("reelDifficultyRate", .001);
		this.reelDifficultyDiscount = config.getDouble("reelDifficultyDiscount", .003);
		this.baseObstacleDifficulty = config.getDouble("baseObstacleDifficulty", 2.0);
		this.baseObstacleDeviation = config.getDouble("baseObstacleDeviation", 1.0);
		this.obstacleDifficultyRate = config.getDouble("obstacleDifficultyRate", .05);
		this.obstacleDifficultyDiscount = config.getDouble("obstacleDifficultyDiscount", 0.0);
		this.baseTimePerDifficulty = config.getDouble("baseTimePerDifficulty", 2.0);
		this.maxDifficultyRange = config.getInt("maxDifficultyRange", 20);
		this.timeDiscount = config.getDouble("timeDiscount", 0.0025);
		this.extraFishPerLevel = config.getDouble("extraFishPerLevel", 0.2);
		this.qualityRate = config.getDouble("qualityRate", 0.01);
		this.restrictLower = config.getBoolean("restrictLower", false);
		
		this.fishRecords = new LinkedList<>();
		if (!config.contains("fish")) {
			QuestManagerPlugin.logger.warning("Didn't find any fish table"
					+ "for FishingSkill even though it's enabled!");
			return;
		} else {
			ConfigurationSection sex = config.getConfigurationSection("fish");
			RegionManager rManager = QuestManagerPlugin.questManagerPlugin.getEnemyManager();
			for (String key : sex.getKeys(false)) {
				if (key.startsWith("==")) {
					continue;
				}
				
				try {
					fishRecords.add(new FishRecord(
							sex.getInt(key + ".difficulty"), sex.getDouble(key + ".weight", 1.0), 
							sex.getItemStack(key + ".icon", new ItemStack(Material.RAW_FISH)),
							rManager.getRegion(sex.contains(key + ".region") ? 
									(sex.get(key + ".region") == null ? null : ((LocationState) sex.get(key + ".region")).getLocation())
									: null)));
				} catch (Exception e) {
					e.printStackTrace();
					QuestManagerPlugin.logger.warning("Skipping that one! ^");
				}
			}
		}
				
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
		FishingGui.setFishingSkill(this);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			YamlWriter writer = new YamlWriter();
			
			writer.addLine("enabled", true, Lists.newArrayList("Whether or not this skill is allowed to be used.",  "true | false"))
				.addLine("startingLevel", 0, Lists.newArrayList("The level given to players who don't have this skill yet", "[int]"))
				.addLine("difficultyPerRow", 15, Lists.newArrayList("How many difficulty levels are required to add", "another row of water to the minigame", "[int] greater than 0", "Will enforce cap of 5"))
				.addLine("baseReelDifficulty", 0.03, Lists.newArrayList("Average reel change (out of 1) per second", "higher levels are harder", "[double] ratio out of 1. Game fails when", "the total number goes above 1 or below 0"))
				.addLine("baseReelDeviation", 0.02, Lists.newArrayList("Standard deviation of change in reel change. Larger", "values make for more sporadic, harder games", "[double] ratio out of 1. Google [Std. Deviation]"))
				.addLine("letRate", 3.0, Lists.newArrayList("Multiplier applied when the line is being let", "[double] straight multiplier"))
				.addLine("reelDifficultyRate", 0.001, Lists.newArrayList("Amount added per level under fish difficulty to", "reel difficulty", "[double] ratio out of 1"))
				.addLine("reelDifficultyDiscount", .003, Lists.newArrayList("Discount per level given to reel difficulty", "[double] .01 is 1% total discount"))
				.addLine("baseObstacleDifficulty", 2.0, Lists.newArrayList("Average time between obstacle creation. Smaller", "values make for harder games ", "[double] time in seconds, rounds to nearest 0.05"))
				.addLine("baseObstacleDeviation", 1.0, Lists.newArrayList("Standard deviation on time between obstacles", "[double] time in seconds. Google [Std. Deviation]"))
				.addLine("obstacleDifficultyRate", .05, Lists.newArrayList("Time deducted from average time between obstacles", "per level under the fish difficulty", "[double] time in seconds"))
				.addLine("obstacleDifficultyDiscount", 0.0, Lists.newArrayList("Discount per level given to obstacle time", "[double] .01 is 1%"))
				.addLine("baseTimePerDifficulty", 2.0, Lists.newArrayList("Time per difficulty of the fish the player must", "play the minigame and hold it for", "[double] time in seconds"))
				.addLine("maxDifficultyRange", 20, Lists.newArrayList("Biggest gap between player and fish difficulty", "that will be allowed through RANDOM catch", "algorithm", "[int] larger than 0"))
				.addLine("timeDiscount", 0.0025, Lists.newArrayList("Discount taken off total time per skill level", "[double] .01 is 1%"))
				.addLine("extraFishPerLevel", 0.2, Lists.newArrayList("How many extra fish a level over fish", "difficulty gives. Expected to be a fraction.", "extra fish rounds down. So .8 extra fish is 0", "[double] fish per level. .1 is 1/10 a fish"))
				.addLine("qualityRate", 0.01, Lists.newArrayList("Bonus to quality per fishing skill level", "[double] .01 is 1%"))
				.addLine("restrictLower", false, Lists.newArrayList("Prevent players from getting fish that are too low level"));
			
			
			Map<String, Map<String, Object>> map = new HashMap<>();
			Map<String, Object> sub = new HashMap<>();
			
			sub.put("difficulty", 10);
			sub.put("weight", 1.0);
			ItemStack item = new ItemStack(Material.RAW_FISH);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("Trout");
			meta.setLore(Lists.newArrayList("Moderate fish of low rarity.", "Good for cooking"));
			item.setItemMeta(meta);
			sub.put("icon", item);
			map.put("Trout", sub);
			
			sub = new HashMap<>();
			sub.put("difficulty", 20);
			sub.put("weight", 0.75);
			item = new ItemStack(Material.RAW_FISH, 1, (short) 1);
			meta = item.getItemMeta();
			meta.setDisplayName("Salmon");
			meta.setLore(Lists.newArrayList("Smaller fish popular", "in Western Culture"));
			item.setItemMeta(meta);
			sub.put("icon", item);
			map.put("Salmon", sub);
			
			writer.addLine("fish", map, Lists.newArrayList("List of fish and their difficulties", "Plan difficulties carefully, as players that are", "at a level with no fish in range (maxDifficultyRange)", "are stuck forever!", "name: {difficulty: [int], icon: [itemstack], weight: [double], region: [null|regionName]}"));
			
			try {
				writer.save(configFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return writer.buildYaml();
		}
		
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		return config;
	}
	
	@EventHandler
	public void onPlayerFish(PlayerFishEvent e) {
		if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
			return;
		}
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getWorlds()
				.contains(e.getPlayer().getWorld().getName())) {
			return;
		}
		

		//e.setCancelled(true);
		e.getCaught().remove();
		
		QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(e.getPlayer());
		int level = qp.getSkillLevel(this);
		
		FishRecord record = getFish(level, e.getHook().getLocation());
		if (record == null) {
			e.getPlayer().sendMessage(BAD_RANGE_MESSAGE);
			return;
		}
		
		FishEvent event = new FishEvent(qp, new QualityItem(record.icon), record.difficulty);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled()) {
			return;
		}
		
		int deltaDifficulty = Math.max(0, record.difficulty - level);
		double obstacleTime, obstacleDeviation, completionTime;
		int rows, amount;
		float reelDifficulty, reelDeviation;
		
		
		obstacleTime = baseObstacleDifficulty + (deltaDifficulty * obstacleDifficultyRate);
		obstacleDeviation = baseObstacleDeviation;
		completionTime = baseTimePerDifficulty * record.difficulty;
		rows = (int) Math.min(5, Math.max(1, 1 + ((int) record.difficulty / (int) difficultyPerRow)));
		amount = 1 + (int) Math.floor(extraFishPerLevel * Math.max(level - record.difficulty, 0));
		reelDifficulty = baseReelDifficulty + (reelDifficultyRate * deltaDifficulty);
		reelDeviation = baseReelDeviation;
		
		////////Modifer Code - Move to eventhandler if mechs moved out of skill/////////
		
		event.setObstacleDifficultyModifier(event.getObstacleDifficultyModifier()
				- (obstacleDifficultyDiscount * level));
		event.setReelDifficultyModifier(event.getReelDifficultyModifier()
				- (reelDifficultyDiscount * level));
		event.setTimeModifier(event.getTimeModifier() - (timeDiscount * level));
		event.setQualityModifier(event.getQualityModifier() + (level * qualityRate));
		
		////////////////////////////////////////////////////////////////////////////////
		
		//apply modifiers
		obstacleTime *= event.getObstacleDifficultyModifier();
		obstacleDeviation *= event.getObstacleDeviationModifier();
		completionTime *= event.getTimeModifier();
		reelDifficulty *= event.getReelDifficultyModifier();
		reelDeviation *= event.getReelDeviationModifier();
		
		QualityItem reward = new QualityItem(record.icon.clone());
		reward.getUnderlyingItem().setAmount(amount);
		reward.setQuality(reward.getQuality() * event.getQualityModifier());
		
		FishingGui gui = new FishingGui(e.getPlayer(), reward, record.difficulty, rows,
				reelDifficulty, reelDeviation, letRate, obstacleTime, obstacleDeviation, completionTime);
		InventoryMenu menu = new InventoryMenu(qp, gui);
		QuestManagerPlugin.questManagerPlugin.getInventoryGuiHandler().showMenu(e.getPlayer(), menu);
		gui.start();
		
	}

	/**
	 * Finds and returns a fish (if one exists) within {@link #maxDifficultyRange} of the provided
	 * difficulty.
	 * @return A fish record within the provided limits, or null if none were found
	 */
	private FishRecord getFish(int difficulty, Location bobberLocation) {
		if (fishRecords.isEmpty()) {
			return null;
		}
		
		List<FishRecord> applicable = new LinkedList<>();
		double sum = 0;
		Collections.shuffle(fishRecords);
		for (FishRecord record : fishRecords) {
			if ((!restrictLower && record.difficulty < difficulty) ||
					Math.abs(record.difficulty - difficulty) <= maxDifficultyRange)
			if (record.region == null || record.region.isIn(bobberLocation)) {
				//return record;
				applicable.add(record);
				sum += record.weight;
			}
		}
		
		//have a list of applicable. get random based on weight
		if (applicable.isEmpty())
			return null;

		double index = RANDOM.nextDouble() * sum;
		for (FishRecord record : applicable) {
			index -= record.weight;
			if (index <= 0)
				return record;
		}
		
		//shouldn't ever get here. If it does, just return last in list
		return applicable.get(applicable.size() - 1);
	}
}
