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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.skyisland.questmanager.QuestManagerPlugin;
import com.skyisland.questmanager.configuration.utils.YamlWriter;
import com.skyisland.questmanager.magic.MagicRegenEvent;
import com.skyisland.questmanager.player.QuestPlayer;
import com.skyisland.questmanager.player.skill.LogReducedSkill;
import com.skyisland.questmanager.player.skill.Skill;
import com.skyisland.questmanager.player.skill.event.CombatEvent;
import com.skyisland.questmanager.player.skill.event.MagicCastEvent;
import com.skyisland.questmanager.player.utils.SpellHolder;
import com.skyisland.questmanager.ui.menu.action.ForgeAction;

/**
 * Skill governing combat with a weapon in main hand, magic in offhand
 *
 */
public class MagicWeaverSkill extends LogReducedSkill implements Listener {
	
	public static final String CONFIG_NAME = "MagicWeaver.yml";
	
	public static final String MODIFIER_NAME = "QuestManager SpellWeaver bonus";

	public Type getType() {
		return Skill.Type.COMBAT;
	}
	
	public String getName() {
		return "Magic Weaver";
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.RECORD_10);
	}
	
	public String getDescription(QuestPlayer player) {
		String ret = ChatColor.WHITE + "Those skilled in Magic Weaving wield both melee weapon and magic in unison, "
				+ "with devastating results.";
		
		int lvl = player.getSkillLevel(this);
		if (lvl < apprenticeLevel) {
			ret += "\n\n" + ChatColor.RED + "Chance to hit: " + (int) (-rateDecrease * (apprenticeLevel - lvl)) + "%";
		}
		
		int rate = (int) (100 * lvl * manaRate);
		ret += "\n" + ChatColor.GREEN + "Mana Recharge: " + (rate < 0 ? "-" : "+") + rate + "%" + ChatColor.RESET;

		rate = (int) (100 * lvl * meleeRate);
		ret += "\n" + ChatColor.GREEN + "Swing Speed: " + (rate < 0 ? "-" : "+") + rate + "%" + ChatColor.RESET;
		
		return ret;
	}

	@Override
	public int getStartingLevel() {
		return startingLevel;
	}
	
	@Override
	public String getConfigKey() {
		return "Magic_Weaver";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof MagicWeaverSkill);
	}
	
	private int startingLevel;
	
	private double manaRate;
	
	private double meleeRate;
	
	private int apprenticeLevel;
	
	private double rateDecrease;
	
	private int levelPenalty;
	
	public MagicWeaverSkill() {
		File configFile = new File(QuestManagerPlugin.questManagerPlugin.getDataFolder(),
				QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillPath() + CONFIG_NAME);
		YamlConfiguration config = createConfig(configFile);
		
		if (!config.getBoolean("enabled", true)) {
			return;
		}
		
		this.startingLevel = config.getInt("startingLevel", 0);
		this.manaRate = config.getDouble("manaRatePerLevel", 0.01);
		this.meleeRate = config.getDouble("swingSpeedPerLevel", 0.01);
		this.levelPenalty = config.getInt("levelPenalty", 3);
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			YamlWriter writer = new YamlWriter();
			
			writer.addLine("enabled", true, Lists.newArrayList("Whether or not this skill is allowed to be used.", "true | false"))
				.addLine("startingLevel", 0, Lists.newArrayList("The level given to players who don't have this skill yet", "[int]"))
				.addLine("manaRatePerLevel", 0.01, Lists.newArrayList("Bonus given to mana recharge rate per skill level.", "As a decimal percent -- 0.50 is 50%", "[double]"))
				.addLine("swingSpeedPerLevel", 0.01, Lists.newArrayList("Bonus swing speed per skill level.", "As a decimal percent -- 0.50 is 50%", "[double]"))
				.addLine("levelPenalty", 3, Lists.newArrayList("This skill gains experience whenever either magic or melee is used when", "the proper equipement is equiped. Because of this, it makes", "sense for it to gain less experience than usual.", "This is the penalty to 'action level' given. This", "MUST be greater than the general skill lower limit!", "[int]"));
			
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
	public void onCombat(CombatEvent e) {
		Player p = e.getPlayer().getPlayer().getPlayer();
		
		if (!ForgeAction.Repairable.isRepairable(e.getWeapon().getType())
				|| (e.getWeapon() == null || !SpellHolder.SpellHolderDefinition.isHolder(e.getOtherItem()))) {
			Skill.setAttributeModifier(p.getAttribute(Attribute.GENERIC_ATTACK_SPEED), MODIFIER_NAME, 0);
			return;
		}
		
		int lvl = e.getPlayer().getSkillLevel(this);
		
		//all we do is modify swing speed (so reduce cooldown) when swinging
		double rate = (lvl * meleeRate); //we'll subtract this.
		//bonus attack damage
		Skill.setAttributeModifier(p.getAttribute(Attribute.GENERIC_ATTACK_SPEED), MODIFIER_NAME, rate);
		
		//System.out.println("reported attack speed: " + p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getBaseValue() + " -> " + p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getValue());
		
		this.perform(e.getPlayer(), lvl - levelPenalty, e.isMiss()); //only get a 'cause miss' if this skill caused it 
		
	}
	

	
	@EventHandler
	public void onMagicCastEvent(MagicCastEvent e) {
		Player p = e.getPlayer().getPlayer().getPlayer();
		
		if (!ForgeAction.Repairable.isRepairable(p.getInventory().getItemInMainHand().getType())
				|| (p.getInventory().getItemInOffHand() == null || !SpellHolder.SpellHolderDefinition.isHolder(p.getInventory().getItemInOffHand()))) {
			return;
		}
		
		//just need to award experience
		this.perform(e.getPlayer(), e.getPlayer().getSkillLevel(this) - levelPenalty, e.isFail());
	}
	
	@EventHandler
	public void onManaRegenEvent(MagicRegenEvent e) {
		if (!(e.getEntity() instanceof QuestPlayer)) {
			return;
		}
		
		QuestPlayer qp = (QuestPlayer) e.getEntity();
		double rate = qp.getSkillLevel(this) * manaRate;
		
		//e.setAmount((int) (e.getAmount() * rate));
		e.setModifier(e.getModifier() + rate);
	}
}
